package com.daifukuoc.wrxj.custom.ebs.host.communication.sac;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Properties;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.comport.ComPort;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.ConnConfigKeys;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageManager;

/**
 * This takes care of everything regarding server socket communication plus transaction handling.
 * 
 * @author LK
 *
 */
public class SACClientSocketHandler implements Runnable {

    public static final String DEFAULT_PORT_NAME = "HostPort";
    public static final String DEFAULT_INTEGRATOR_NAME = "HostIntegrator";

    public static final int CONNECTION_TIMEOUT = 20 * 1000;
    public static final int READ_TIMEOUT = 100;
    public static final String COM_PORT_STATUS_CHANGE = "StatusChange";
    public static final int BUFFER_SIZE = 100 * 1024; // 100 KB

    private static final String WRX_RUN_MODE = Application.getString(WarehouseRx.RUN_MODE);
    private static final int MINIMUM_RETRY_INTERVAL = 1;
    
    private static final int MIN_PORT_NUMBER = 1024;
    private static final int MAX_PORT_NUMBER = 65535;

    private final Logger logger;
    private final MessageService messageService;

    private final String portName;
    private final String integratorName;
    private final String ipAddress;
    private final int portNumber;
    private final int keepAliveTimeout;
    private final int retryInterval;
    private final int ackTimeout;
    private final int ackMaxRetry;
    private final boolean useSTXETX;

    private SACTransactionHandler transactionHandler = null;
    private Socket connectedSocket = null;
    private InputStream socketInputStream = null;
    private OutputStream socketOutputStream = null;
    private Integer maxNumberOfExecution = null;

    private boolean shouldStop = false;
    private int prevStatus = ComPort.COM_PORT_STATE_UNKNOWN;

    /**
     * Instantiate a new ServerSocketHandler
     * 
     * @param connectionProperties
     * @param logger The logger
     * @param messageService The message service used in publishing events
     * @throws Exception 
     */
    public SACClientSocketHandler(Properties connectionProperties, Logger logger, MessageService messageService) throws Exception {

        this(connectionProperties, logger, messageService, null, null, null);
    }

    /**
     * A constructor used only for unit testing, so do not use this for production.
     * 
     * @param connectionProperties The properties that include host ip, port and so on
     * @param logger The logger
     * @param messageService The message service used in publishing events
     * @param transactionHandler The transaction handler
     * @param connectedSocket The connected socket
     * @param maxNumberOfExecution The maximum number of execution
     * @throws Exception 
     */
    public SACClientSocketHandler(Properties connectionProperties, Logger logger, MessageService messageService,
            SACTransactionHandler transactionHandler, Socket connectedSocket, Integer maxNumberOfExecution) throws Exception {

        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CREATE);

        this.logger = logger;
        this.messageService = messageService;

        // Port name
        String portName = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.PORT_NAME.getValue());
        if (portName != null && !portName.isEmpty()) {
            this.portName = portName;
        } else {
            this.portName = DEFAULT_PORT_NAME;
        }

        // Integrator name
        String integratorName = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.INTEGRATOR_NAME.getValue());
        if (integratorName != null && !integratorName.isEmpty()) {
            this.integratorName = integratorName;
        } else {
            this.integratorName = DEFAULT_INTEGRATOR_NAME;
        }

        // IP address
        String ipAddress = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.IP_ADDRESS.getValue());
        if (ipAddress != null && !ipAddress.isEmpty()) {
            this.ipAddress = ipAddress;
        } else {
            this.ipAddress = "";
        }

        // Port number
        String portNumber = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.PORT_NUMBER.getValue());
        int portNumberValue = -1;
        if (portNumber != null && !portNumber.isEmpty()) {
            try {
                portNumberValue = Integer.parseInt(portNumber);
            } catch (NumberFormatException e) {
            }
        }
        this.portNumber = portNumberValue;

        // Keep alive timeout
        // - (2 x receive keep alive interval) is used as keep alive timeout
        String keepAliveInterval = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.KEEP_ALIVE_INTERVAL.getValue());
        int keepAliveIntervalValue = -1;
        if (keepAliveInterval != null && !keepAliveInterval.isEmpty()) {
            try {
                keepAliveIntervalValue = Integer.parseInt(keepAliveInterval);
            } catch (NumberFormatException e) {
            }
        }
        this.keepAliveTimeout = keepAliveIntervalValue * 2;

        // Retry interval
        String retryInterval = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.RETRY_INTERVAL.getValue());
        int retryIntervalValue = -1;
        if (retryInterval != null && !retryInterval.isEmpty()) {
            try {
                retryIntervalValue = Integer.parseInt(retryInterval);
            } catch (NumberFormatException e) {
            }
        }
        if (retryIntervalValue < MINIMUM_RETRY_INTERVAL ) {
            retryIntervalValue = MINIMUM_RETRY_INTERVAL;
        }
        this.retryInterval = retryIntervalValue;

        // Ack timeout
        String ackTimeout = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.ACK_TIMEOUT.getValue());
        int ackTimeoutValue = -1;
        if (ackTimeout != null && !ackTimeout.isEmpty()) {
            try {
                ackTimeoutValue = Integer.parseInt(ackTimeout);
            } catch (NumberFormatException e) {
            }
        }
        this.ackTimeout = ackTimeoutValue;

        // Ack max retry
        String ackMaxRetry = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.ACK_MAX_RETRY.getValue());
        int ackMaxRetryValue = -1;
        if (ackMaxRetry != null && !ackMaxRetry.isEmpty()) {
            try {
                ackMaxRetryValue = Integer.parseInt(ackMaxRetry);
            } catch (NumberFormatException e) {
            }
        }
        this.ackMaxRetry = ackMaxRetryValue;

        // STX/ETX flag
        String useSTXETX = connectionProperties == null ? null
                : connectionProperties.getProperty(ConnConfigKeys.USE_STXETX.getValue());
        if (useSTXETX == null || useSTXETX.isEmpty()) {
            this.useSTXETX = true;
        } else {
            this.useSTXETX = Boolean.valueOf(useSTXETX);
        }

        // Transaction handler
        if (transactionHandler == null) {
            // Create a new transaction handler if not provided
            this.transactionHandler = new SACTransactionHandler(this.portName, this.integratorName, this.ackTimeout,
                    this.ackMaxRetry, this.useSTXETX, this.logger, this.messageService,
                    Factory.create(SACMessageManager.class),
                    new TimeoutChecker());
        } else {
            // Put the provided transaction handler
            // Used only for unit testing
            this.transactionHandler = transactionHandler;
        }

        // Used only for unit testing
        this.connectedSocket = connectedSocket;

        // Used only for unit testing
        if (connectedSocket != null) {
            try {
                setSocketStream(connectedSocket);
            } catch (IOException e) {
            }
        }

        // Used only for unit testing
        this.maxNumberOfExecution = maxNumberOfExecution;

        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CREATING);
    }

    /**
     * Returns the TransactionHandler
     * 
     * @return The TransactionHandler
     */
    public SACTransactionHandler getTransactionHandler() {
        return transactionHandler;
    }

    /**
     * This is called when ACPPort.shutdown() is called to stop the server socket
     * 
     */
    public void shutdown() {
        publishCommEventIfRequired(ComPort.COM_PORT_STATE_STOP);

        shouldStop = true;

        publishCommEventIfRequired(ComPort.COM_PORT_STATE_STOPPING);
    }

    @Override
    public void run() {
        byte[] receivedByteArray = new byte[BUFFER_SIZE];
        ByteBuffer receivedByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        SACMessageManager messageProcessor = new SACMessageManager();

        int numberOfReceivedMessages = 0;
        int sequenceNumberOfExecution = 0;

        while (!shouldStop) {
            // Run this block only up to the given number of executions
            // Used only for unit testing
            if (maxNumberOfExecution != null) {
                sequenceNumberOfExecution++;
                if (sequenceNumberOfExecution > maxNumberOfExecution.intValue()) {
                    break;
                }
            }

            // Wait for a new connection
            if (connectedSocket == null) {
                if (isPossibleToStart()) {
                    try {
                        logAllDebug("Trying to connect to " + getRemote());
                        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CONNECTING);

                        connectedSocket = new Socket(ipAddress, portNumber);
                        connectedSocket.setSoTimeout(READ_TIMEOUT); // timeout for read()
                        setSocketStream(connectedSocket);

                        logAllDebug("The new connection established: " + connectedSocket.getLocalAddress() + ":"
                                + connectedSocket.getLocalPort() + " <--> "
                                + connectedSocket.getInetAddress().getHostAddress() + ":" + connectedSocket.getPort());
                        transactionHandler.connected(keepAliveTimeout, socketOutputStream);

                        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CONNECTED);
                    } catch (Exception e) {
                        closeConnection();
                        publishCommEventIfRequired(ComPort.COM_PORT_STATE_ERROR);
                        sleepBeforeRetry();
                        continue;
                    }
                } else {
                    logError("Impossible to create a client socket at " + portNumber + " for " + portName
                            + " due to invalid configuration");
                    closeConnection();
                    publishCommEventIfRequired(ComPort.COM_PORT_STATE_ERROR);
                    sleepBeforeRetry();
                    continue;
                }
            }

            publishCommEventIfRequired(ComPort.COM_PORT_STATE_RUNNING);

            // Read
            try {
                numberOfReceivedMessages = 0;
                int receivedBytesCount = socketInputStream.read(receivedByteArray);
                if (receivedBytesCount > 0) {
                    logAllDebug("Incoming Message:"
                            + MessageUtil.encodeHexString(receivedByteArray, 0, receivedBytesCount));
                   // logger.logRxByteCommunication(receivedByteArray, 0, receivedBytesCount);
                    numberOfReceivedMessages = processReceivedBytes(receivedByteArray, receivedBytesCount,
                            receivedByteBuffer, messageProcessor, transactionHandler);
                  //KR:  logAllDebug("Received " + numberOfReceivedMessages + " inbound message(s) at " + getRemote());
                } else if (receivedBytesCount < 0) {
                    logAllDebug("The connection at " + getRemote()
                            + " seems to be disconnected by client");
                    closeConnection();
                    continue;
                }
            } catch (SocketTimeoutException e) {
                // read() timed out, so we don't need to do anything
            } catch (Exception e) {
                logAllDebug("Failed to read on the connection at " + getRemote() + " : "
                        + e.getMessage());
                closeConnection();
                continue;
            }

            // Transaction processing
            if (!transactionHandler.process()) {
                closeConnection();
                continue;
            }

            // Keep alive time out checking
            if (transactionHandler.isKeepAliveTimedOut()) {
                closeConnection();
                continue;
            }
        }

        // Now the main while loop has finished, let's clear the connection and the server socket
        closeConnection();
        publishCommEventIfRequired(ComPort.COM_PORT_STATE_STOPPED);
    }

    /**
     * Get the connection target description
     * 
     * @return the description of target
     */
    private String getRemote() {
        return ipAddress + ":" + portNumber + " for " + portName;
    }

    /**
     * Checks if it's possible to start a new server socket
     * 
     * @return True if possible
     */
    private boolean isPossibleToStart() {
        // ip address
        if (ipAddress == null || ipAddress.isEmpty()) {
            logSocketError("Not possible to start a socket due to invalid ip address(null or emptyt)");
            return false;
        }
        if (!isValidPort(portNumber)) {
            logSocketError("Not possible to start a socket due to invalid port number: " + portNumber);
            return false;
        }
        if (keepAliveTimeout <= 0) {
            logSocketError("Not possible to start a socket due to invalid keep alive timeout: " + keepAliveTimeout);
            return false;
        }
        if (retryInterval < 0) {
            logSocketError("Not possible to start a socket due to invalid retry interval: " + retryInterval);
            return false;
        }
        if (ackTimeout < 0) {
            logSocketError("Not possible to start a socket due to invalid ack timeout: " + ackTimeout);
            return false;
        }
        if (ackMaxRetry < 0) {
            logSocketError("Not possible to start a socket due to invalid ack max retry: " + ackMaxRetry);
            return false;
        }
        return true;
    }

    /**
     * Checks if the given port number is valid for server socket
     * 
     * @param portNumber The configured port number
     * @return True if valid
     */
    private boolean isValidPort(int portNumber) {
        if (portNumber < MIN_PORT_NUMBER || portNumber > MAX_PORT_NUMBER) {
            return false;
        }
        return true;
    }

    /**
     * When retrying to start a new socket because of connection issue, we need to take a break for retryInterval
     * 
     */
    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryInterval * 1000);
        } catch (InterruptedException e1) {
        }
    }

    /**
     * Closes the input stream and connected socket if necessary
     */
    private void closeConnection() {
        logSocketDebug("Closing the connection connected to " + getRemote());

        closeSocketStream();
        closeConnectedSocket();
    }

    /**
     * Closes the input/output stream if necessary
     */
    private void closeSocketStream() {
        logSocketDebug("Closing the input/output streams linked the connection to " + getRemote());

        try {
            if (socketInputStream != null) {
                socketInputStream.close();
            }
        } catch (Exception e) {

        } finally {
            socketInputStream = null;
        }

        try {
            if (socketOutputStream != null) {
                socketOutputStream.close();
            }
        } catch (Exception e) {

        } finally {
            socketOutputStream = null;
        }
    }

    /**
     * Closes the connected socket if necessary
     */
    private void closeConnectedSocket() {
        try {
            if (connectedSocket != null) {
                connectedSocket.close();
            }
        } catch (Exception ex) {
        } finally {
            connectedSocket = null;
        }
    }

    private void setSocketStream(Socket connectedSocket) throws IOException {
        closeSocketStream();

        this.socketInputStream = connectedSocket.getInputStream();
        this.socketOutputStream = connectedSocket.getOutputStream();
    }

    /**
     * Publishes the current socket status - ACPPort will use this event to update its controller status
     * 
     * @param status The status int value(See ComPort)
     */
    private void publishCommEventIfRequired(int status) {
        if (prevStatus != status) {
            int statusToPublish = -1;
            switch (status) {
            case ComPort.COM_PORT_STATE_RUNNING:
                statusToPublish = ControllerConsts.STATUS_RUNNING;
                break;
            case ComPort.COM_PORT_STATE_STOPPING:
                statusToPublish = ControllerConsts.STATUS_STOPPING;
                break;
            case ComPort.COM_PORT_STATE_STOPPED:
                statusToPublish = ControllerConsts.STATUS_STOPPED;
                break;
            case ComPort.COM_PORT_STATE_ERROR:
                statusToPublish = ControllerConsts.STATUS_ERROR;
                break;
            default:
                break;
            }
            if (statusToPublish != -1) {
                messageService.publishEvent(portName, WRX_RUN_MODE, COM_PORT_STATUS_CHANGE, statusToPublish,
                        MessageEventConsts.COMM_EVENT_TYPE, MessageEventConsts.COMM_EVENT_TYPE_TEXT + portName);
            }
            prevStatus = status;
        }
    }

    /**
     * This will split the received bytes to multiple messages considering STX/ETX + header + body structure. -
     * receivedByteArray has the received bytes up to receivedBytesCount from the index 0 - receivedByteBuffer should be
     * kept as it will be used for the following write operation - Split messages will be stored into transactionHandler
     * 
     * @param receivedByteArray The byte array written by socket's input stream
     * @param receivedBytesCount The number of bytes read from socket's input stream
     * @param receivedByteBuffer The byte buffer to hold byte array not split yet
     * @param messageProcessor The message processor to convert byte array
     * @param transactionHandler The transaction handler to store the split message into the internal queue
     * @return The number of split messages
     */
    private int processReceivedBytes(byte[] receivedByteArray, int receivedBytesCount,
            ByteBuffer receivedByteBuffer, SACMessageManager messageProcessor,
            TransactionHandler transactionHandler) {

        int numberOfReceivedMessages = 0;
        int savedPosition = -1;
        byte[] rawHeaderByteArray = null;
        SACMessageHeader processedHeader = null;
        byte[] rawWholeMessageByteArray = null;

        // Write the received byte array to byte buffer for buffering
        receivedByteBuffer.put(receivedByteArray, 0, receivedBytesCount);

        // Flip before reading after writing operation is done
        receivedByteBuffer.flip();

        // Now receivedByteBuffer is ready for reading
        while (receivedByteBuffer.hasRemaining()) {
            // Get the header length considering STX
            int headerLengthIncludingSTX = SACControlMessage.MSG_HEADER_LEN;
            if (useSTXETX) {
                headerLengthIncludingSTX += 1;
            }

            // Check if we have enough bytes for header including STX
            if (receivedByteBuffer.remaining() < headerLengthIncludingSTX) {
                // We need to read more bytes for header, so return for further reading without clearing
                // receivedByteBuffer
                receivedByteBuffer.compact();
                // Return for further reading
                return numberOfReceivedMessages;
            }

            // Save the current position
            savedPosition = receivedByteBuffer.position();

            // Wait until STX is received
            if (useSTXETX) {
                byte theFirstByte = receivedByteBuffer.get();
                if (theFirstByte != SACControlMessage.STX) {
                    // Discard the byte
                    continue;
                }
            }

            // Read the header
            rawHeaderByteArray = new byte[SACControlMessage.MSG_HEADER_LEN];
            receivedByteBuffer.get(rawHeaderByteArray, 0, SACControlMessage.MSG_HEADER_LEN);

            processedHeader = messageProcessor.processReceivedHeader(rawHeaderByteArray);
            if (processedHeader == null) {
                // Discard all of header
                continue;
            }

            // Get the header length considering ETX
            int bodyLengthIncludingETX = processedHeader.getMsgLength() - SACControlMessage.MSG_HEADER_LEN;
            if (useSTXETX) {
                bodyLengthIncludingETX += 1;
            }

            // Check if we have enough bytes for body including ETX
            if (receivedByteBuffer.remaining() < bodyLengthIncludingETX) {
                // Move back to the start position of message
                receivedByteBuffer.position(savedPosition);
                // We need to read more bytes for body, so return for further reading without clearing
                // receivedByteBuffer
                receivedByteBuffer.compact();
                // Return for further reading
                return numberOfReceivedMessages;
            }

            // Read the entire message including the header
            rawWholeMessageByteArray = new byte[processedHeader.getMsgLength()];
            // - Copy the processed header
            System.arraycopy(rawHeaderByteArray, 0, rawWholeMessageByteArray, 0, SACControlMessage.MSG_HEADER_LEN);
            // - Read the message body and append it to the header
            receivedByteBuffer.get(rawWholeMessageByteArray, SACControlMessage.MSG_HEADER_LEN,
                    processedHeader.getMsgLength() - SACControlMessage.MSG_HEADER_LEN);

            // Check if the message ends with ETX
            if (useSTXETX) {
                byte theLastByte = receivedByteBuffer.get();
                if (theLastByte != SACControlMessage.ETX) {
                    // Discard the whole message
                    rawWholeMessageByteArray = null;
                    continue;
                }
            }

            transactionHandler.addInboundMessage(rawWholeMessageByteArray);
            numberOfReceivedMessages++;
        }

        receivedByteBuffer.clear();

        return numberOfReceivedMessages;
    }

    /*
     * The below 5 methods are copied from SocketComPort.java to provide the similar logging
     */
    private void logError(String s) {
        logger.logError(s);
        logTxDebug("**** " + s + " ****");
    }

    private void logTxDebug(String s) {
        logger.logTxByteCommunication(s.getBytes(), 0, s.length());
    }

    private void logSocketError(String s) {
        logError("  # SOCKET #  " + s);
    }

    private void logSocketDebug(String s) {
        logger.logDebug("  # SOCKET #  " + s);
    }

    private void logAllDebug(String s) {
        logSocketDebug(s);
        logTxDebug(s);
    }

}
