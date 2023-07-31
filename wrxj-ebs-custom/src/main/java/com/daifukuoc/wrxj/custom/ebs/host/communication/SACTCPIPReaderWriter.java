package com.daifukuoc.wrxj.custom.ebs.host.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.daifukuamerica.TCPIPCommException;
import com.daifukuamerica.TCPIPLogger;
import com.daifukuamerica.TCPIPReadEvent;
import com.daifukuamerica.impl.ConnectionType;
import com.daifukuamerica.impl.TCPIPConstants;
import com.daifukuamerica.impl.TCPIPLoggerImpl;
import com.daifukuoc.wrxj.custom.ebs.host.messages.MessageUtil;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACControlMessage;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageManager;
import com.daifukuoc.wrxj.custom.ebs.host.messages.SACMessageHeader;

public class SACTCPIPReaderWriter extends java.lang.Thread implements TCPIPReaderWriterInterface {

    private static final String HEARTBEAT_STRING = "HeartBeatString";
    // KR: new vars to handle new SAC interface
    private String msMessageTxt = null;
    private SACMessageManager mpSACMessageHandler = null;

    // KR: OLD local
    private byte mbStartOfText;
    private byte mbEndOfText;
    private String msClientAddress;
    private String msHeartBeatString;
    private String msLogDir;
    private String msServerIP;
    private int mnClientRetryIntvl;
    private int mnConnectPort;
    private final int STOP_RUNAWAY_COUNT;
    private TCPIPLogger mpLogger;
    private SocketChannel mpCommChannel;
    private Selector mpSelector;
    private boolean mzUseAcks;
    private boolean useSTXETX;
    private volatile boolean mzThreadStop;
    private volatile TCPIPReadEvent mpTCPReadHandler;
    private volatile SACTCPIPSocketCloseEvent mpTCPClientCloseHandler;
    
    private LocalDateTime connectionEstablished;
    private LocalDateTime lastKeepAliveReceived;

    public SACTCPIPReaderWriter() {
        this.mbStartOfText = (byte) 2;
        this.mbEndOfText = (byte) 3;
        this.msClientAddress = "";
        this.msHeartBeatString = "";
        this.msLogDir = "";
        this.msServerIP = "";
        this.mnClientRetryIntvl = 6;
        this.mnConnectPort = 0;
        this.STOP_RUNAWAY_COUNT = 5;
        this.mpSelector = null;
        this.mzUseAcks = false;
        this.useSTXETX = false;
        this.mzThreadStop = false;
    }

    public SACTCPIPReaderWriter(Properties ipConfigProp, SocketChannel ipCommChannel, TCPIPLogger ipLogger)
            throws TCPIPCommException {

        this();

        if (ipCommChannel != null && this.msClientAddress.isEmpty()) {
            InetSocketAddress vpAddr = (InetSocketAddress) ipCommChannel.socket().getRemoteSocketAddress();
            this.msClientAddress = vpAddr.getAddress().getHostAddress();
        }
        this.setName("Thread_" + this.msClientAddress);
        this.mpCommChannel = ipCommChannel;

        this.initConfigSettings(ipConfigProp);
        if (ipLogger == null) {
            this.mpLogger = new TCPIPLoggerImpl("com.daifukuamerica.impl", this.msLogDir,
                    "Client_" + this.msClientAddress + "_%g.log");
        } else {
            this.mpLogger = ipLogger;
        }

        if (mbStartOfText == SACControlMessage.STX && mbEndOfText == SACControlMessage.ETX) {
            this.useSTXETX = true;
        }
    }

    public SACTCPIPReaderWriter(Properties ipConfigProp, TCPIPLogger ipLogger) throws TCPIPCommException {
        this(ipConfigProp, null, ipLogger);
    }

    public SACTCPIPReaderWriter(Properties ipConfigProp) throws TCPIPCommException {
        this(ipConfigProp, null, null);
    }

    public SACTCPIPReaderWriter(Properties ipConfigProp, SocketChannel ipCommChannel) throws TCPIPCommException {
        this(ipConfigProp, ipCommChannel, null);
    }

    /* Protected & Public methods */

    public byte[] getHeartBeatMessage() {
        setSACMessageHandler();
        int iEquipmentNo = MessageUtil.EQUIPMENT_ID; // TODO: get iEquipmentNo
        short iSequNo = 1; // TOOO: get sequence no
        return mpSACMessageHandler.createKeepAliveMessage(iEquipmentNo, iSequNo);
    }

    public void useAckNak(boolean izUseAcks) {
        this.mzUseAcks = izUseAcks;
    }

    public String getClientIPAddress() {
        return this.msClientAddress;
    }

    public <E extends TCPIPReadEvent> void registerReadEvent(E ipEventHandler) {
        this.mpTCPReadHandler = ipEventHandler;
    }

    public <E extends SACTCPIPSocketCloseEvent> void registerCloseEvent(E ipEventHandler) {
        this.mpTCPClientCloseHandler = ipEventHandler;
    }

    public boolean isConnectionAlive() {
        return this.mpCommChannel != null ? this.mpCommChannel.isConnected() : false;
    }

    public void connToServer() throws TCPIPCommException {
        if (this.mpCommChannel == null) {
            try {
                this.mpCommChannel = SocketChannel.open();
                this.mpCommChannel.configureBlocking(false);
                this.mpCommChannel.connect(new InetSocketAddress(this.msServerIP, this.mnConnectPort));
                this.mpCommChannel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.valueOf(true));
            } catch (IOException var5) {
                throw new TCPIPCommException("Error configuring Socket Channel...", var5);
            }
            // KR: changed the name of the following ??
            int inTick = 0;

            try {
                for (int vnTick = 0; !this.mpCommChannel.finishConnect(); vnTick += 2000) {
                    if (vnTick >= this.mnClientRetryIntvl) {
                        this.mpLogger.logErrorMessage(
                                "Try Connecting to Server " + this.msServerIP + " port " + this.mnConnectPort);

                        try {
                            Thread.sleep(2000L);
                        } catch (InterruptedException var3) {
                            ;
                        }
                    }
                    // KR:??
                    inTick = vnTick;
                }
                
                connectionEstablished = LocalDateTime.now();
                this.mpLogger.logDebugMessage(">>> SAC <<< connected: " + connectionEstablished);

            } catch (IOException var4) {
                throw new TCPIPCommException(
                        "Error connecting to host...Failed connection after " + inTick / 2000 + " tries!", var4);
            }
        }
    }

    public ByteBuffer sendMessage(byte[] mabMessage) throws TCPIPCommException {

        int messageLengthToSend = mabMessage.length;
        if (useSTXETX) {
            messageLengthToSend = mabMessage.length + 2;
        }
        ByteBuffer vpWriteBuf = ByteBuffer.allocate(messageLengthToSend);
        if (useSTXETX) {
            vpWriteBuf.put(SACControlMessage.STX);
        }
        vpWriteBuf.put(mabMessage);
        if (useSTXETX) {
            vpWriteBuf.put(SACControlMessage.ETX);
        }
        vpWriteBuf.flip();
        try {
            byte[] vpTemp = vpWriteBuf.array();

            for (int vnSent = this.mpCommChannel.write(vpWriteBuf); vnSent < vpTemp.length; vnSent += this.mpCommChannel
                    .write(vpWriteBuf)) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var7) {
                    ;
                }
            }
            return vpWriteBuf;
        } catch (IOException var8) {
            throw new TCPIPCommException("Error writing to socket!  Host may be down.", var8);
        }
    }

    public void run() {
        ByteBuffer vpReadByteBuf = ByteBuffer.allocate(512);

        try {
            // check if it is not initialized yet
            if (this.mpCommChannel == null) {
                this.mpLogger.logErrorMessage("Reader Thread is not initialized.");
                return;
            }

            this.mpCommChannel.configureBlocking(false);
            this.mpSelector = Selector.open();
            this.mpCommChannel.register(this.mpSelector, 1);
        } catch (IOException var19) {
            this.mpLogger.logErrorMessage("Selector error in reader thread... Reader Thread terminating.");
            return;
        }

        while (!this.mzThreadStop) {
            try {
                this.mpSelector.select();
            } catch (IOException var18) {
                ;
            }

            if (!this.mzThreadStop && !this.mpCommChannel.socket().isClosed()) {
                Set vpSelectedKeySet = this.mpSelector.selectedKeys();
                Iterator vpIter = vpSelectedKeySet.iterator();

                while (vpIter.hasNext()) {
                    SelectionKey vpSelectedKey = (SelectionKey) vpIter.next();
                    vpIter.remove();
                    if (vpSelectedKey.isValid() && vpSelectedKey.isReadable()) {
                        SocketChannel vpSockChannel = (SocketChannel) vpSelectedKey.channel();

                        try {
                            if (vpSockChannel.read(vpReadByteBuf) != -1) {
                                this.processChannelData(vpReadByteBuf);
                            } else {
                                this.mzThreadStop = true;
                            }
                        } catch (IOException var15) {
                            this.mzThreadStop = true;
                            this.mpLogger.logErrorMessage("Connection dropped due to exception! " + var15.getMessage());
                        } catch (Exception var16) {
                            this.mpLogger.logErrorMessage("Data Error!" + var16.getMessage());
                        } finally {
                            if (this.mzThreadStop) {
                                mpLogger.logDebugMessage(">>> SAC <<< ReaderWriter's run(): calling closeSocket() as mzThreadStop is true");
                                this.closeSocket();
                            }
                        }
                    }
                }
            } else {
                try {
                    this.mpSelector.close();
                } catch (Exception var17) {
                    ;
                }

                this.mzThreadStop = true;
            }
        }

    }

    public void stopThread() {
        mpLogger.logDebugMessage(">>> SAC <<< ReaderWriter's stopThread(): setting mzThreadStop to true");
        this.mzThreadStop = true;
        mpLogger.logDebugMessage(">>> SAC <<< ReaderWriter's stopThread(): calling interrupt()");
        this.interrupt();
        mpLogger.logDebugMessage(">>> SAC <<< ReaderWriter's stopThread(): calling closeSocket()");
        this.closeSocket();
    }

    /* private methods */

    private void processChannelData(ByteBuffer ipReadBuf) throws IOException {
        byte[] vbHeader = null;
        byte[] vbMessage = null;
        int savedPosition = 0;

        // Initialise the message handler if not set yet
        setSACMessageHandler();

        // Switch to read mode
        ipReadBuf.flip();
        while (ipReadBuf.hasRemaining()) {

            // Get the header length considering STX
            int headerLengthIncludingSTX = SACControlMessage.MSG_HEADER_LEN;
            if (useSTXETX) {
                headerLengthIncludingSTX += 1;
            }

            // Check if we have enough bytes for header including STX
            if (ipReadBuf.remaining() < headerLengthIncludingSTX) {
                this.mpLogger.logDebugMessage("Message header is not fully received yet");
                // If more bytes to read for header, switch to write mode after removing the processed bytes if any
                ipReadBuf.compact();
                // Return for further reading
                return;
            }

            // Save the current position
            savedPosition = ipReadBuf.position();

            // Wait until STX is received
            if (useSTXETX) {
                byte theFirstByte = ipReadBuf.get();
                if (theFirstByte != SACControlMessage.STX) {
                    this.mpLogger.logDebugMessage(
                            "Expected STX, but actually " + MessageUtil.encodeHexString(theFirstByte) + " received");
                    continue;
                }
            }

            // Read the header
            vbHeader = new byte[SACControlMessage.MSG_HEADER_LEN];
            ipReadBuf.get(vbHeader, 0, SACControlMessage.MSG_HEADER_LEN);
            SACMessageHeader header = mpSACMessageHandler.processReceivedHeader(vbHeader);

            // Check if the received header is valid
            if (header == null) {
                this.mpLogger.logDebugMessage("Invalid header received: " + MessageUtil.encodeHexString(vbHeader));
                continue;
            }

            // Get the header length considering ETX
            int bodyLengthIncludingETX = header.getMsgLength() - SACControlMessage.MSG_HEADER_LEN;
            if (useSTXETX) {
                bodyLengthIncludingETX += 1;
            }

            // Check if we have enough bytes for body including ETX
            if (ipReadBuf.remaining() < bodyLengthIncludingETX) {
                this.mpLogger.logDebugMessage("Message body is not fully received yet");
                this.mpLogger
                        .logDebugMessage("Expected: " + header.getMsgLength() + ", Current: " + ipReadBuf.remaining());
                // Move back to the start position of message
                ipReadBuf.position(savedPosition);
                // Switch to write mode after removing the processed bytes
                ipReadBuf.compact();
                // Return for further reading
                return;
            }

            // Read the entire message including the header
            vbMessage = new byte[header.getMsgLength()];
            // - First of all, copy the processed header
            System.arraycopy(vbHeader, 0, vbMessage, 0, SACControlMessage.MSG_HEADER_LEN);
            // - Read the message body and append it to the byte array
            ipReadBuf.get(vbMessage, SACControlMessage.MSG_HEADER_LEN,
                    header.getMsgLength() - SACControlMessage.MSG_HEADER_LEN);

            // Check if the message ends with ETX
            if (useSTXETX) {
                byte theLastByte = ipReadBuf.get();
                if (theLastByte != SACControlMessage.ETX) {
                    this.mpLogger.logDebugMessage(
                            "Expected ETX, but actually " + MessageUtil.encodeHexString(theLastByte) + " received");
                    continue;
                }
            }

            // Now pass the received message to the handler for processing
            mpSACMessageHandler.setMessage(vbMessage);
            logCommMessage(mpSACMessageHandler.getReceivedDataFromHost());
            if (mpSACMessageHandler.processReceivedMessage()) {
                // Update the last keep alive received
                if (mpSACMessageHandler.getMessageHeader() != null && 
                        mpSACMessageHandler.getMessageHeader().getMsgType() == SACControlMessage.KEEPALIVE_MSG_TYPE) {
                    lastKeepAliveReceived = LocalDateTime.now();
                    this.mpLogger.logDebugMessage(">>> SAC <<< keep alive received: " + lastKeepAliveReceived);
                }
                msMessageTxt = mpSACMessageHandler.getMessageTxt();
                logCommMessage(msMessageTxt);
                this.mpLogger.logDebugMessage("The received message: " + MessageUtil.encodeHexString(vbMessage));
                
                this.mpTCPReadHandler.receivedData(this.mpCommChannel, msMessageTxt);
            }
        }

        this.mpLogger.logDebugMessage("Finished processing the received bytes, so clearing the receive buffer");
        ipReadBuf.clear();
        mpSACMessageHandler.clear();
    }

    private void logCommMessage(String strMsg) {
        this.mpLogger.logCommMessage("<---", strMsg);
    }

    private void initConfigSettings(Properties ipConfigProp) throws TCPIPCommException {
        this.msHeartBeatString = ipConfigProp.getProperty(HEARTBEAT_STRING);
        String vsPfx = ipConfigProp.getProperty("MessagePrefix");
        String vsSuf = ipConfigProp.getProperty("MessageSuffix");
        this.mbStartOfText = vsPfx.isEmpty() ? 0 : Byte.decode(vsPfx);
        this.mbEndOfText = vsSuf.isEmpty() ? 0 : Byte.decode(vsSuf);
        String vsConnectionType = ipConfigProp.getProperty("SocketType");
        this.msLogDir = ipConfigProp.getProperty("LogPath");
        this.msServerIP = ipConfigProp.getProperty("ServerIP", "");
        if (!ConnectionType.isServer(vsConnectionType) && this.msServerIP.isEmpty()) {
            throw new TCPIPCommException("Invalid TCP/IP configuration file entry!ServerIP not specified!");
        } else {
            this.mnConnectPort = Integer.parseInt(ipConfigProp.getProperty("ListenPort"));
            if (this.mnConnectPort == 0) {
                throw new TCPIPCommException("Invalid TCP/IP configuration file entry!ListenPort not specified!");
            } else {
                this.mnClientRetryIntvl = 1000 * Integer.parseInt(ipConfigProp.getProperty("ClientRetryInterval", "6"));
            }
        }
    }

    private void closeSocket() {
        try {
            if (this.mpCommChannel != null) {
                this.mpCommChannel.close();
            }

            if (this.mpSelector != null) {
                this.mpSelector.close();
            }

            if (this.mpTCPClientCloseHandler != null) {
                this.mpTCPClientCloseHandler.socketCloseEvent(this);
            }

            this.mpCommChannel = null;
        } catch (IOException var2) {
            ;
        }

    }

    private void setSACMessageHandler() {
        if (mpSACMessageHandler == null) {
            mpSACMessageHandler = new SACMessageManager();
        }
    }

    public LocalDateTime lastKeepAliveReceived() {
        return lastKeepAliveReceived;
    }

    public LocalDateTime connectionEstablished() {
        return connectionEstablished;
    }
}
