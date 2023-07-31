package com.daifukuoc.wrxj.custom.ebs.host.communication;

import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPConnectionEvent;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPServerComms;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.host.communication.TCPIPBaseLoggerImpl;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.messages.ExpectedReceiptResponseMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;

/**
 * Host TCP/IP sever transport class
 * 
 * @author KR
 *
 */
public class SACTCPServerTransport implements Transporter, Runnable {
    public static final String TCP_SERVER_TRANSPORT_NAME = "TCPServerTransport";

    protected Properties mpConnProperties;

    /** Server Listen Port. */
    protected int mnListenPort = 0;
    /** The name or IP of this host/Server socket */
    protected String msHostName = null;
    protected String msHostIP = null; // KR: Added this to separate the name with IP
    /*
     * The name of the collaborator (HostMessageIntegrator) that will be notified when we receive a message.
     */
    protected String msCollaborator = "";
    protected String msCommGroup = "";
    protected boolean mzUseAcks;
    protected boolean mzUseHeartBeats;
    protected volatile boolean mzConnEstablished = false;
    protected Thread mpMainTransportThread;

    protected TCPIPLogger mpLogger;
    protected TCPIPServerComms mpServComm;
    protected SACTCPIPReaderWriter mpReadWriteThread;
    protected Controller mpSystemGateway;
    // Database related declarations.
    protected StandardHostServer mpHostServer;

    public SACTCPServerTransport(Controller ipSystemGateway, String isHostControllerName, String isCommGroup)
            throws HostCommException {
        msCommGroup = isCommGroup;
        mpSystemGateway = ipSystemGateway;
        mpHostServer = Factory.create(StandardHostServer.class, TCP_SERVER_TRANSPORT_NAME);
        msCollaborator = Application.getString(Application.HOSTCFG_DOMAIN + isHostControllerName + ".Collaborator");

        initConnectionProperties();
    }

    /**
     * Init connection properties
     */
    private void initConnectionProperties() {
        msHostName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostName");
        msHostIP = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostIP");
        mnListenPort = Application.getInt(Application.HOSTCFG_DOMAIN + msCommGroup + ".ListenPort");
        mzUseAcks = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseAcks", false);
        mzUseHeartBeats = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseHeartBeats", false);

        String vsHBMesg = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.HEART_BEAT_MSG);
        String vsMesgPrefix = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_PREFIX);
        String vsMesgSuffix = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_SUFFIX);

        mpConnProperties = new Properties();
        mpConnProperties.setProperty(TCPIPConstants.SERVER_IP, msHostIP); // KR: changed to IP instead of HostName
        mpConnProperties.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(mnListenPort));
        mpConnProperties.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.SERVER.getValue());
        mpConnProperties.setProperty(TCPIPConstants.HEART_BEAT_MSG, (vsHBMesg == null) ? "" : vsHBMesg);
        mpConnProperties.setProperty(TCPIPConstants.MESSAGE_PREFIX, (vsMesgPrefix == null) ? "" : vsMesgPrefix);
        mpConnProperties.setProperty(TCPIPConstants.MESSAGE_SUFFIX, (vsMesgSuffix == null) ? "" : vsMesgSuffix);

    }

    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public int getTransportModel() {
        return (Transporter.SERVER_TRANSPORT);
    }

    @Override
    public void setCommPort(int inPort) {
        mnListenPort = inPort;
    }

    @Override
    public void setLogger(Logger ipWrxjLogger) {
        mpLogger = new TCPIPBaseLoggerImpl(ipWrxjLogger);
    }

    @Override
    public void startTransporter() {
        mpMainTransportThread = new Thread(this);
        mpMainTransportThread.start();
    }

    @Override
    public boolean isTransporterAlive() {
        return mpMainTransportThread != null && mpMainTransportThread.isAlive()
                && mpMainTransportThread.getState() != Thread.State.TERMINATED;
    }

    @Override
    public void run() {
        final Properties vpProperties = getConnectionProperties();
        mpServComm = Factory.create(TCPIPServerComms.class, vpProperties, mpLogger);
        try {
            // What we do when a client connects to us.
            mpServComm.registerConnectionEvents(new TCPIPConnectionEvent() {
                @Override
                public void connectionHandler(SocketChannel ipClientChannel) {
                    try {
                        /*
                         * Dispose of old connection thread if it's still there.
                         */
                        if (isHostReachable()) {
                            closeHostConnection();
                        }

                        SACTCPIPReadEventImpl vpReadEvent = Factory.create(SACTCPIPReadEventImpl.class, msHostName,
                                msCommGroup, mpLogger);
                        vpReadEvent.useAckNak(mzUseAcks);
                        // Set up to let HostMessageIntegrator
                        // know when we receive a message.
                        vpReadEvent.setupCollaboratorNotification(mpSystemGateway, msCollaborator);

                        SACTCPIPSocketCloseEvent vpCloseEvt = Factory.create(SACTCPIPSocketCloseEventImpl.class,
                                msCommGroup, mpSystemGateway, mpLogger);

                        /*
                         * Setup the Client Thread and its Event handler. KR: the SACTCPIPReaderWriter handles read and
                         * write from and to PORT (SAC)
                         */
                        mpReadWriteThread = Factory.create(SACTCPIPReaderWriter.class, vpProperties, ipClientChannel,
                                mpLogger);
                        mpReadWriteThread.registerReadEvent(vpReadEvent);
                        mpReadWriteThread.registerCloseEvent(vpCloseEvt);
                        mpReadWriteThread.start();
                        mzConnEstablished = true;
                    } catch (Exception exc) {
                        mpLogger.logErrorMessage("Client Connection dropped due to Exception!", exc);
                        closeHostConnection();
                    }
                }
            });

            mpServComm.connectionWait(); // Loop and wait for each new connection.
        } catch (TCPIPCommException ce) {
            mpLogger.logErrorMessage("TCP/IP Server Communication Error", ce);
        }
    }

    @Override
    public void sendHeartBeat() throws HostCommException {
        if (mzUseHeartBeats) {
            try {
                if (mpReadWriteThread != null) // if connection established ....
                {
                    // KR: modified this to build the hear-beat message based on the new SAC interface
                    byte[] vsHBMesg = mpReadWriteThread.getHeartBeatMessage();
                    mpReadWriteThread.sendMessage(vsHBMesg);
                }
            } catch (TCPIPCommException ex) {
                throw new HostCommException("Error establishing connection to Server " + "Socket from Client.", ex);
            }
        }

    }

    /**
     * Loads the data from queue (database- WRXTOHOST table) and send to SAC This method called from HostController
     * checkForMessageSendEvent when the HOST_MESG_SEND_EVENT_TYPE is received
     */
    @Override
    public synchronized int sendMessages(HostServerDelegate ipOutDelegate, StandardHostServer ipHostServer)
            throws DBException, HostCommException {
        int vnMessageCount = 0;
        WrxToHostData vpWrxToHostData = null;

        // KR - check if sac still connected
        if (!this.isHostReachable()) {
            this.mpLogger.logErrorMessage("Error: The Host is not reachable");
            return 0;
        }

        try {
            // Give delegate information related to
            // the task it will perform.
            ipOutDelegate.setInfo(msHostName);
            ipOutDelegate.setReadLockInfo(true);

            // Read outbound message from WrxToHost table.
            vpWrxToHostData = (WrxToHostData) mpHostServer.getOldestDataQueueMessage(ipOutDelegate);
            if (vpWrxToHostData != null) {

                // KR:Process the message and get it ready to send to SAC as byte[].
                // Take a look at the xml property in the WRXtoHostData .....
                byte[] msgAsByte = getMessageAsByteArray(vpWrxToHostData);
                if (msgAsByte != null) {
                    vpWrxToHostData.setMessageBytes(msgAsByte);
                    mpReadWriteThread.sendMessage(vpWrxToHostData.getMessageBytes());

                    // Mark WRx-J message as processed.
                    ipOutDelegate.setInfo(vpWrxToHostData);
                    ipHostServer.markMessageAsProcessed(ipOutDelegate);
                    vnMessageCount++;
                } else {
                    this.mpLogger.logErrorMessage("Failed to get the byte[] from msg:" + vpWrxToHostData.getMessage());
                }
                vpWrxToHostData = null;
            }
        } catch (TCPIPCommException hce) {
            mpLogger.logErrorMessage("Error Sending message to host!", hce);
        }
        return (vnMessageCount);
    }

    @Override
    public void closeHostConnection() {
        mzConnEstablished = false;
        if (mpReadWriteThread != null)
            mpReadWriteThread.stopThread();
    }

    /**
     * Stop this thread.
     */
    @Override
    public void stopTransporter() {
        closeHostConnection();
        /*
         * It is possible that mpServComm is not initialised yet if they start the HostController and then stop it
         * immediately before running this thread.
         */
        if (mpServComm != null)
            mpServComm.stopServer();
    }

    /**
     * {@inheritDoc}. This method tests the remote connection to see if it's valid. This method is only called to check
     * the outbound socket. If the configuration parameter <code>HostConfig.UseHeartBeats</code> is set to
     * <code>false</code> then this method always returns <code>true.</code>
     * 
     * @return <code>boolean</code> of <code>true</code> only if remote sockets are still accessible.
     */
    @Override
    public boolean isHostReachable() {
        boolean vzRtn;

        if (mpReadWriteThread == null || !mpReadWriteThread.isAlive() || !mpReadWriteThread.isConnectionAlive())
            vzRtn = mzConnEstablished = false;
        else
            vzRtn = mzConnEstablished = true;

        return (vzRtn);
    }

    @Override
    public boolean isConnectionEstablished() {
        return (mzConnEstablished);
    }

    /**
     * Method loads configurations into a Property file class so that the tcp/ip comms. library can be used.
     *
     * @return reference to Properties object.
     */
    protected Properties getConnectionProperties() {

        if (mpConnProperties == null) {
            initConnectionProperties();
        }

        return (mpConnProperties);
    }

    // FIXME: Server and Client should use the same implementation, right?
    byte[] getMessageAsByteArray(WrxToHostData vpWrxToHostData) {

        if (vpWrxToHostData == null || vpWrxToHostData.getMessage().isEmpty()) {
            this.mpLogger.logErrorMessage("Invalid WrxToHostData msg");
            return null;
        }

        byte[] mbMsg = null;

        if (vpWrxToHostData.getMessageIdentifierEnum() == MessageOutNames.EXPECTED_RECEIPT_COMPLETE) {
            String[] commaSeparated = vpWrxToHostData.getMessage().split(";");
            if (commaSeparated.length == ExpectedReceiptResponseMessage.NUMBER_OF_FIELDS) {
                mbMsg = MessageUtil.buildExpectedReceiptResponseMessage(vpWrxToHostData.getMessage());
            } else {
                this.mpLogger.logErrorMessage("Invalid EXPECTED_RECEIPT_COMPLETE msg" + vpWrxToHostData.getMessage());
            }
        } else if (vpWrxToHostData.getMessageIdentifierEnum() == MessageOutNames.STORE_COMPLETE) {
            String[] commaSeparated = vpWrxToHostData.getMessage().split(";");
            if (commaSeparated.length > 5) {
                mbMsg = MessageUtil.buildStoreCompleteNotifyMessage(vpWrxToHostData.getMessage());
            } else {
                this.mpLogger.logErrorMessage("Invalid Store complete notify msg" + vpWrxToHostData.getMessage());
            }
        } else if (vpWrxToHostData.getMessageIdentifierEnum() == MessageOutNames.FLIGHT_DATA_UPDATE) {
            mbMsg = MessageUtil.buildFlightDataUpdateResponseMessage(vpWrxToHostData.getMessage());
        } else if (vpWrxToHostData.getMessageIdentifierEnum() == MessageOutNames.ORDER_COMPLETE) {
            // equipment id: 1111, hard-coded???
            mbMsg = MessageUtil.buildRetrievalOrderResponseMessage(vpWrxToHostData.getMessage());
        } else {
            this.mpLogger.logErrorMessage("Unimplemented message: " + vpWrxToHostData.getMessage());
        }

        return mbMsg;
    }

    @Override
    public int retransmitPendingMessages(HostServerDelegate hostOutDelegate, StandardHostServer hostServer, int maxRetry)
            throws DBException, HostCommException {
        List<WrxToHostData> pendingMessages = null;
        int sentMessageCount = 0;

        if (!isHostReachable()) {
            mpLogger.logErrorMessage("Error: The Host is not reachable");
            return 0;
        }
        
        try {
            hostOutDelegate.setInfo(msHostName);
            hostOutDelegate.setReadLockInfo(true);
            pendingMessages = (List<WrxToHostData>) mpHostServer.getAllPendingMessages(hostOutDelegate, 60);
            for (WrxToHostData pendingMessage : pendingMessages) {
                if (pendingMessage.getRetryCount() >= maxRetry) {
                    // If retry count >= max, update acked to 3 to give up more retransmission
                    hostOutDelegate.setInfo(pendingMessage);
                    hostServer.markMessageAsAckFailed(hostOutDelegate);
                } else {
                    // If not, retransmit the message
                    byte[] msgAsByte = getMessageAsByteArray(pendingMessage);
                    if (msgAsByte != null) {
                        pendingMessage.setMessageBytes(msgAsByte);
                        mpReadWriteThread.sendMessage(pendingMessage.getMessageBytes());
                        
                        hostOutDelegate.setInfo(pendingMessage);                        
                        hostServer.markMessageAsRetransmitted(hostOutDelegate);
                        sentMessageCount++;
                    } else {
                        mpLogger.logErrorMessage("Failed to get the byte[] from msg:" + pendingMessage.getMessage());
                    }
                }
            }
        } catch (TCPIPCommException hce) {
            mpLogger.logErrorMessage("Error Sending message to host!", hce);
        }
        
        return sentMessageCount;
    }

    @Override
    public LocalDateTime lastKeepAliveReceived() {
        if (mpReadWriteThread != null) {
            return mpReadWriteThread.lastKeepAliveReceived();
        }
        return null;
    }

    @Override
    public LocalDateTime connectionEstablished() {
        if (mpReadWriteThread != null) {
            return mpReadWriteThread.connectionEstablished();
        }
        return null;
    }
}
