package com.daifukuoc.wrxj.custom.ebs.host.communication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.dataserver.standard.StandardHostServer;
import com.daifukuamerica.wrxj.dataserver.standard.TransactionToken;
import com.daifukuamerica.wrxj.dbadapter.data.WrxToHostData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.host.HostCommException;
import com.daifukuamerica.wrxj.host.HostServerDelegate;
import com.daifukuamerica.wrxj.host.Transporter;
import com.daifukuamerica.wrxj.host.communication.TCPIPBaseLoggerImpl;
import com.daifukuamerica.wrxj.host.messages.MessageOutNames;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;

public class SACTCPClientTransport implements Transporter, Runnable {
    
    public static final String TCP_CLIENT_TRANSPORT_NAME = "TCPClientTransport";
    
    /** Connection Port. */
    protected int mnConnectionPort = 0;
    /** The name or IP of this host/Server socket */
    protected String msHostName = null;
    protected String msHostIP = null; // KR: Added this to separate the name with IP
    /**
     * The name of the collaborator (HostMessageIntegrator) that will be notified when we receive a message.
     */
    protected String msCommGroup;
    protected String msCollaborator = "";
    protected boolean mzUseAcks;
    protected boolean mzUseHeartBeats;
    protected volatile boolean mzConnEstablished = false;

    protected Thread mpClientThread;
    protected TCPIPLogger mpLogger;
    protected SACTCPIPReaderWriter mpReadWriteThread;
    protected Controller mpSystemGateway;
    protected Properties mpConnProperties;

    // Database related declarations.
    protected StandardHostServer mpHostServer;

    public SACTCPClientTransport(Controller ipSystemGateway, String isHostControllerName, String isCommGroup)
            throws HostCommException {
        mpSystemGateway = ipSystemGateway;
        msCommGroup = isCommGroup;
        initConnectionProperties();

        mpHostServer = Factory.create(StandardHostServer.class, TCP_CLIENT_TRANSPORT_NAME);

        msCollaborator = Application.getString(Application.HOSTCFG_DOMAIN + isHostControllerName + ".Collaborator");
    }

    @Override
    public void setCommPort(int inPort) {
        mnConnectionPort = inPort;
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@inheritDoc}
     */
    @Override
    public int getTransportModel() {
        return (Transporter.CLIENT_TRANSPORT);
    }

    @Override
    public void setLogger(Logger ipWrxJLogger) {
        mpLogger = new TCPIPBaseLoggerImpl(ipWrxJLogger);
    }

    /**
     * The main purpose of this run is to connect to the host and start up a client thread to manage that connection.
     * <b>Note:</b> If there is already a connection by chance, and someone tries to start another thread, do nothing!
     */
    @Override
    public void run() {
        if (mzConnEstablished) {
            return;
        }

        try {
            /*
             * Handle any data that comes off the socket as a Read Event.
             */
            SACTCPIPReadEventImpl vpReadEvent = Factory.create(SACTCPIPReadEventImpl.class, msHostName, msCommGroup,
                    mpLogger);
            vpReadEvent.useAckNak(mzUseAcks);
            // Configure notification for HostMessageIntegrator
            // for when messages arrive.
            vpReadEvent.setupCollaboratorNotification(mpSystemGateway, msCollaborator);
            // Event handler for when host connection is closed.
            SACTCPIPSocketCloseEvent vpCloseEvt = Factory.create(SACTCPIPSocketCloseEventImpl.class, msCommGroup,
                    mpSystemGateway, mpLogger);
            /*
             * Register Event Handler with the Reader Thread and Start it.
             */
            mpReadWriteThread = Factory.create(SACTCPIPReaderWriter.class, getConnectionProperties(), mpLogger);
            mpReadWriteThread.registerReadEvent(vpReadEvent);
            mpReadWriteThread.registerCloseEvent(vpCloseEvt);
            mpReadWriteThread.connToServer();
            mpReadWriteThread.start();
            mzConnEstablished = true;

            try {
                mpReadWriteThread.join();
            } catch (InterruptedException ie) {
                mzConnEstablished = false;
            }
        } catch (TCPIPCommException exc) {
            mpLogger.logErrorMessage("Client Connection Error to " + msHostName, exc);
            mzConnEstablished = false;
        }
    }

    @Override
    public void sendHeartBeat() throws HostCommException {
        if (mzUseHeartBeats) {
            byte[] vsHBMesg = mpReadWriteThread.getHeartBeatMessage();
            try {
                mpReadWriteThread.sendMessage(vsHBMesg);
            } catch (TCPIPCommException ex) {
                throw new HostCommException("Error establishing connection to Server " + "Socket from Client.", ex);
            }
        }
    }

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
        TransactionToken vpTransaction = null;
        try {
            // Give delegate information related to
            // the task it will perform.
            ipOutDelegate.setInfo(msHostName);
            ipOutDelegate.setReadLockInfo(true);
            // Read outbound message from WrxToHost
            // table.
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
    public void startTransporter() throws HostCommException {
        mpClientThread = new Thread(this);
        mpClientThread.start();
    }

    @Override
    public void closeHostConnection() {
        mzConnEstablished = false;
        if (mpReadWriteThread != null)
            mpReadWriteThread.stopThread();
    }

    /**
     * This is a rehash of the closeHostConnection method. This method is however necessary for compatibility with the
     * TCP/IP Server Transporter.
     */
    @Override
    public void stopTransporter() {
        closeHostConnection();
    }

    @Override
    public boolean isTransporterAlive() {
        return mpClientThread != null && mpClientThread.isAlive()
                && mpClientThread.getState() != Thread.State.TERMINATED;
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
        boolean vzRtn = true;

        if (mzUseHeartBeats) {
            if (mpReadWriteThread == null || !mpReadWriteThread.isAlive() || !mpReadWriteThread.isConnectionAlive())
                vzRtn = mzConnEstablished = false;
            else
                vzRtn = mzConnEstablished = true;
        }

        return (vzRtn);
    }

    @Override
    public boolean isConnectionEstablished() {
        return (mzConnEstablished);
    }

    protected Properties getConnectionProperties() {

        if (mpConnProperties == null) {
            initConnectionProperties();
        }

        return (mpConnProperties);
    }

    private void initConnectionProperties() {
        msHostName = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostName");
        msHostIP = Application.getString(Application.HOSTCFG_DOMAIN + msCommGroup + ".HostIP");
        mzUseAcks = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseAcks", false);
        mzUseHeartBeats = Application.getBoolean(Application.HOSTCFG_DOMAIN + msCommGroup + ".UseHeartBeats", false);
        mnConnectionPort = Application.getInt(Application.HOSTCFG_DOMAIN + msCommGroup + ".ListenPort");

        String vsHBMesg = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.HEART_BEAT_MSG);
        String vsMesgPrefix = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_PREFIX);
        String vsMesgSuffix = Application
                .getString(Application.HOSTCFG_DOMAIN + msCommGroup + "." + TCPIPConstants.MESSAGE_SUFFIX);

        mpConnProperties = new Properties();

        mpConnProperties.setProperty(TCPIPConstants.SERVER_IP, msHostIP);
        mpConnProperties.setProperty(TCPIPConstants.LISTEN_PORT, Integer.toString(mnConnectionPort));
        mpConnProperties.setProperty(TCPIPConstants.SOCKET_TYPE, ConnectionType.CLIENT.getValue());
        mpConnProperties.setProperty(TCPIPConstants.HEART_BEAT_MSG, (vsHBMesg == null) ? "" : vsHBMesg);
        mpConnProperties.setProperty(TCPIPConstants.MESSAGE_PREFIX, vsMesgPrefix);
        mpConnProperties.setProperty(TCPIPConstants.MESSAGE_SUFFIX, vsMesgSuffix);

    }

    /* private methods */

    // FIXME: Server and Client should use the same implementation, right?
    byte[] getMessageAsByteArray(WrxToHostData vpWrxToHostData) {

        if (vpWrxToHostData == null || vpWrxToHostData.getMessage().isEmpty()) {
            this.mpLogger.logErrorMessage("Invalid WrxToHostData msg");
            return null;
        }

        byte[] mbMsg = null;

        if (vpWrxToHostData.getMessageIdentifierEnum() == MessageOutNames.EXPECTED_RECEIPT_COMPLETE) {
            String[] commaSeparated = vpWrxToHostData.getMessage().split(";");
            if (commaSeparated.length == 9) {
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
            mbMsg = MessageUtil.buildRetrievalOrderResponseMessage(vpWrxToHostData.getMessage());
        } else {
            this.mpLogger.logErrorMessage("Unimplemented message: " + vpWrxToHostData.getMessage());
        }

        return mbMsg;
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
