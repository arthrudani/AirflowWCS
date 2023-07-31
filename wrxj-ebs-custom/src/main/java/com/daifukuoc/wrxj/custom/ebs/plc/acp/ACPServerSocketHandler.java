package com.daifukuoc.wrxj.custom.ebs.plc.acp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

import com.daifukuamerica.wrxj.WarehouseRx;
import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.comport.ComPort;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.ipc.MessageService;
import com.daifukuamerica.wrxj.log.Logger;
import com.daifukuoc.wrxj.custom.ebs.communication.TimeoutChecker;
import com.daifukuoc.wrxj.custom.ebs.communication.TransactionHandler;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder.StandardMessageEncoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

/**
 * This takes care of everything regarding server socket communication plus transaction handling.
 * 
 * @author LK
 *
 */
public class ACPServerSocketHandler implements Runnable {

    private static final String WRX_RUN_MODE = Application.getString(WarehouseRx.RUN_MODE);

    public static final int CONNECTION_TIMEOUT = 20 * 1000;
    public static final int READ_TIMEOUT = 100;
    public static final int DEFAULT_RETRY_INTERVAL = 5;
    public static final String COM_PORT_STATUS_CHANGE = "StatusChange";
    public static final int BUFFER_SIZE = 100 * 1024; // 100 KB
    
    private static final int MIN_PORT_NUMBER = 1024;
    private static final int MAX_PORT_NUMBER = 65535;

    private final PortData portData;
    private final Logger logger;
    private final MessageService messageService;

    private final String portName;
    private final int portNumber;
    private final int keepAliveTimeout;
    private final int retryInterval;
    private final boolean useSTXETX;

    private ACPTransactionHandler transactionHandler = null;
    private ServerSocket serverSocket = null;
    private Socket connectedSocket = null;
    private InputStream socketInputStream = null;
    private OutputStream socketOutputStream = null;
    private Integer maxNumberOfExecution = null;

    private boolean shouldStop = false;
    private int prevStatus = ComPort.COM_PORT_STATE_UNKNOWN;

    /**
     * Instantiate a new ServerSocketHandler
     * 
     * @param portData PortData = current port configured in port table
     * @param logger The logger
     * @param messageService The message service used in publishing events
     * @throws Exception 
     */
    public ACPServerSocketHandler(PortData portData, Logger logger, MessageService messageService) throws Exception {

        this(portData, logger, messageService, null, null, null, null);
    }

    /**
     * A constructor used only for unit testing, so do not use this for production.
     * 
     * @param portData PortData = current port configured in port table
     * @param logger The logger
     * @param messageService The message service used in publishing events
     * @param transactionHandler The transaction handler
     * @param serverSocket The server socket
     * @param connectedSocket The connected socket
     * @param maxNumberOfExecution The maximum number of execution
     * @throws Exception 
     */
    public ACPServerSocketHandler(PortData portData, Logger logger, MessageService messageService,
            ACPTransactionHandler transactionHandler, ServerSocket serverSocket, Socket connectedSocket,
            Integer maxNumberOfExecution) throws Exception {

        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CREATE);

        this.portData = portData;
        this.logger = logger;
        this.messageService = messageService;

        // Port name
        this.portName = this.portData.getPortName();

        // Port number
        String socketNumberString = this.portData.getSocketNumber();
        if (socketNumberString == null || socketNumberString.isEmpty()) {
            logSocketError("Socket number of " + portName + " is NOT configured in port");
            this.portNumber = -1;
        } else {
            int socketNumber = -1;
            try {
                socketNumber = Integer.parseInt(socketNumberString);
            } catch (NumberFormatException e) {
            }
            if (socketNumber >= 0 && isValidPort(socketNumber)) {
                this.portNumber = socketNumber;
            } else {
                this.portNumber = -1;
                if (socketNumber < 0) {
                    logSocketError(
                            "Socket number of " + portName + " can't be converted to a number: " + socketNumberString);
                }
                if (!isValidPort(socketNumber)) {
                    logSocketError(
                            "Socket number of " + portName + " should be >= 1024 and <= 65535: " + socketNumberString);
                }
            }
        }

        // Keep alive timeout
        // - (2 x receive keep alive interval) is used as keep alive timeout
        this.keepAliveTimeout = (this.portData.getRcvKeepAliveInterval() / 1000) * 2;

        // Retry interval
        if (this.portData.getRetryInterval() < PortData.MINIMUM_INTERVAL) {
            logSocketDebug("Invalid Retry Interval: " + this.portData.getRetryInterval() + "(should be >= "
                    + PortData.MINIMUM_INTERVAL
                    + ", so will use the default value: " + DEFAULT_RETRY_INTERVAL + " instead.");
            this.retryInterval = DEFAULT_RETRY_INTERVAL;
        } else {
            this.retryInterval = this.portData.getRetryInterval() / 1000;
        }

        // STX/ETX flag
        this.useSTXETX = this.portData.getEnableWrapping();
        
        // Transaction handler
        if (transactionHandler == null) {
        	
        	int equimpentId = ( ! portData.getDeviceID().isBlank() && portData.getDeviceID().isEmpty() ) ? Integer.getInteger(portData.getDeviceID()) : 9001;
            // Create a new transaction handler if not provided
            // FIXME: Do we need to populate ack timeout from port table?
            this.transactionHandler = new ACPTransactionHandler(portName,equimpentId,  this.keepAliveTimeout, useSTXETX, logger, this.messageService,
                    new StandardMessageProcessor(), new StandardMessageEncoderImpl(),
                    new TimeoutChecker());
        } else {
            // Put the provided transaction handler
            // Used only for unit testing
            this.transactionHandler = transactionHandler;
        }

        // Used only for unit testing
        this.serverSocket = serverSocket;

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
    public ACPTransactionHandler getTransactionHandler() {
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
        StandardMessageProcessor messageProcessor = new StandardMessageProcessor();

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

            // Bind
            if (serverSocket == null) {
                if (isPossibleToStart()) {
                    try {
                        logAllDebug("Creating a server socket at " + portNumber);

                        serverSocket = new ServerSocket(portNumber);
                        serverSocket.setReuseAddress(true);
                        serverSocket.setSoTimeout(CONNECTION_TIMEOUT); // timeout for accept()

                        logAllDebug("The server socket created at " + portNumber + " for " + portName);
                        publishCommEventIfRequired(ComPort.COM_PORT_STATE_CREATED);
                    } catch (Exception e) {
                        logError("Failed to create a server socket at " + portNumber + " for " + portName
                                + ": exception=" + e.getMessage());
                        closeServerSocket();
                        publishCommEventIfRequired(ComPort.COM_PORT_STATE_ERROR);
                        sleepBeforeRetry();
                        continue;
                    }
                } else {
                    logError("Impossible to create a server socket at " + portNumber + " for " + portName
                            + " due to invalid configuration");
                    closeServerSocket();
                    publishCommEventIfRequired(ComPort.COM_PORT_STATE_ERROR);
                    sleepBeforeRetry();
                    continue;
                }
            }

            // Wait for a new connection
            if (connectedSocket == null) {
                try {
                    logAllDebug("Waiting for a new connection at " + portNumber);
                    publishCommEventIfRequired(ComPort.COM_PORT_STATE_CONNECTING);

                    connectedSocket = serverSocket.accept();
                    connectedSocket.setSoTimeout(READ_TIMEOUT); // timeout for read()
                    setSocketStream(connectedSocket);

                    logAllDebug("The new connection established: " + connectedSocket.getLocalAddress() + ":"
                            + connectedSocket.getLocalPort() + " <--> "
                            + connectedSocket.getInetAddress().getHostAddress() + ":" + connectedSocket.getPort());
                    transactionHandler.connected(keepAliveTimeout, socketOutputStream);

                    publishCommEventIfRequired(ComPort.COM_PORT_STATE_CONNECTED);
                } catch (SocketTimeoutException e) {
                    connectedSocket = null;
                    continue;
                } catch (Exception e) {
                    connectedSocket = null;
                    continue;
                }
            }

            publishCommEventIfRequired(ComPort.COM_PORT_STATE_RUNNING);

            // Read
            try {
                numberOfReceivedMessages = 0;
                int receivedBytesCount = socketInputStream.read(receivedByteArray);
                if (receivedBytesCount > 0) {
                    logAllDebug("Incoming message received from " + portName + ":"
                            + MessageUtil.encodeHexString(receivedByteArray, 0, receivedBytesCount));
                    logger.logRxByteCommunication(receivedByteArray, 0, receivedBytesCount);
                    numberOfReceivedMessages = processReceivedBytes(receivedByteArray, receivedBytesCount,
                            receivedByteBuffer, messageProcessor, transactionHandler);
                    logAllDebug("Received " + numberOfReceivedMessages + " inbound message(s) at " + portNumber + " of "
                            + portName);
                } else if (receivedBytesCount < 0) {
                    logAllDebug("The connection at " + portNumber + " of " + portName
                            + " seems to be disconnected by client");
                    closeConnection();
                    continue;
                }
            } catch (SocketTimeoutException e) {
                // read() timed out, so we don't need to do anything
            } catch (Exception e) {
                logAllDebug("Failed to read on the connection at " + portNumber + " of " + portName + ": "
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
        closeServerSocket();
        publishCommEventIfRequired(ComPort.COM_PORT_STATE_STOPPED);
    }

    /**
     * Checks if it's possible to start a new server socket
     * 
     * @return True if possible
     */
    private boolean isPossibleToStart() {
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
     * When retrying to start a new socket because of binding issue, we need to take a break for retryInterval
     * 
     */
    private void sleepBeforeRetry() {
        try {
            Thread.sleep(retryInterval * 1000);
        } catch (InterruptedException e1) {
        }
    }

    /**
     * Closes the server socket if necessary
     */
    private void closeServerSocket() {
        logSocketDebug("Closing the server socket running at " + portName + " for " + portName);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception ex) {
        } finally {
            serverSocket = null;
        }
    }

    /**
     * Closes the input stream and connected socket if necessary
     */
    private void closeConnection() {
        logSocketDebug("Closing the connection connected to " + portName + " for " + portName);

        closeSocketStream();
        closeConnectedSocket();
        transactionHandler.disconnected();
    }

    /**
     * Closes the input/output stream if necessary
     */
    private void closeSocketStream() {
        logSocketDebug("Closing the input/output streams linked the connection to " + portName + " for " + portName);

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
            ByteBuffer receivedByteBuffer, StandardMessageProcessor messageProcessor,
            TransactionHandler transactionHandler) {

        int numberOfReceivedMessages = 0;
        int savedPosition = -1;
        byte[] rawHeaderByteArray = null;
        PLCMessageHeader processedHeader = null;
        byte[] rawWholeMessageByteArray = null;

        // Write the received byte array to byte buffer for buffering
        receivedByteBuffer.put(receivedByteArray, 0, receivedBytesCount);

        // Flip before reading after writing operation is done
        receivedByteBuffer.flip();

        // Now receivedByteBuffer is ready for reading
        while (receivedByteBuffer.hasRemaining()) {
            // Get the header length considering STX
            int headerLengthIncludingSTX = PLCConstants.MSG_HEADER_LEN;
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
            rawHeaderByteArray = new byte[PLCConstants.MSG_HEADER_LEN];
            receivedByteBuffer.get(rawHeaderByteArray, 0, PLCConstants.MSG_HEADER_LEN);
            processedHeader = messageProcessor.processReceivedPLCHeader(rawHeaderByteArray,
                    rawHeaderByteArray.length);

            // Check if the received header is valid
            if (processedHeader == null) {
                // Discard all of header
                continue;
            }

            // Get the header length considering ETX
            int bodyLengthIncludingETX = processedHeader.getMsgLength() - PLCConstants.MSG_HEADER_LEN;
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
            System.arraycopy(rawHeaderByteArray, 0, rawWholeMessageByteArray, 0, PLCConstants.MSG_HEADER_LEN);
            // - Read the message body and append it to the header
            receivedByteBuffer.get(rawWholeMessageByteArray, PLCConstants.MSG_HEADER_LEN,
                    processedHeader.getMsgLength() - PLCConstants.MSG_HEADER_LEN);

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
