package com.daifukuoc.wrxj.custom.ebs.plc;

import java.nio.ByteBuffer;

import com.daifukuamerica.wrxj.clc.ReadOnlyProperties;
import com.daifukuamerica.wrxj.controller.Controller;
import com.daifukuamerica.wrxj.controller.ControllerConsts;
import com.daifukuamerica.wrxj.controller.ControllerCreationException;
import com.daifukuamerica.wrxj.dataserver.standard.StandardDeviceServer;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.device.controls.ControlsPort;
import com.daifukuamerica.wrxj.device.port.PortConsts;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.ipc.MessageEventConsts;
import com.daifukuamerica.wrxj.jdbc.DBConstants;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.log.LogConsts;
import com.daifukuamerica.wrxj.time.SkDateTime;
import com.daifukuamerica.wrxj.timer.RestartableTimerTask;
import com.daifukuamerica.wrxj.util.SKDCConstants;
import com.daifukuoc.wrxj.custom.ebs.dataserver.SystemHBServer;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCConstants;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.PLCMessageHeader;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.encoder.StandardMessageEncoderImpl;
import com.daifukuoc.wrxj.custom.ebs.plc.messages.processor.StandardMessageProcessor;

/**
 * Handles PLC Port communications
 * 
 * @author KR
 *
 */
public class PLCPort extends ControlsPort {

    private StandardMessageProcessor mpMessageProcessor = null;
    private StandardMessageEncoderImpl mpStandardMessageEncoderImpl = null;

    private Boolean ShuttingDownStarted = false;
    private Object lock = new Object();
    // private int msgVersion = 1;//KR: get this from db
    private boolean izEmulation = false; // determines if set to Emulation mode
    private static final int BCS_MAX_SEQNO = 65535;
    private static final int EBS_NO_ACTIVITY_TIMEOUT = 30000;
    private static final int EBS_NO_ACTIVITY_TIMEOUT_CHECK_INTERVAL = 10000;
    private static final int NO_ACTIVITY_TIME = 60000;// 1*30*1000; // 60 seconds
    private static final int LENGTH_OF_DELIMITER = 2;
    private SkDateTime lastRecvdMessageDateTime = new SkDateTime(SKDCConstants.STATUS_DATE_FORMAT);
    private PLCMessageHeader mpBCSMessageHeader = new PLCMessageHeader();
    private byte[] receivedByteBuffer = new byte[PLCConstants.MAX_MSG_LEN]; // KR: Not sure about BCS_MAX_MSG_LEN?

    protected int mnPLCReceivedKeepAliveInterval = EBS_NO_ACTIVITY_TIMEOUT_CHECK_INTERVAL;
    protected int mnExpectedReceivedSequenceNumber = 0;
    protected PLCReceivedKeepAliveTimeout mpPLCReceivedKeepAliveTimeout = new PLCReceivedKeepAliveTimeout();
    protected byte[] mabMsgHeader = { PortConsts.ETX };
    protected SystemHBServer mpSysHBServer = null;

    public String sDeviceID = ""; // the PLC Id in the Device table

    private int headerLength;

    public PLCPort() {
        super();
        this.headerLength = PLCConstants.MSG_HEADER_LEN + (isMessageWrapped() ? 1 : 0);

    }

    /**
     * Extra initialization
     */
    @Override
    public void initialize(String controllerKeyName) {
        super.initialize(controllerKeyName);
        setSequenceLength(PLCConstants.SEQNO_FIELD_LEN);
        setBlockProtocolType(PortConsts.FIXED_LENGTH_BLOCK_PROTOCOL);
        setFixedBlockLength(this.headerLength);

        bufferDataWhenNotRunning = false; // MCM, EBS don't buffer messages

        // Determine if we are in emulation mode
        StandardDeviceServer vpDevServ = Factory.create(StandardDeviceServer.class);

        int index = controllerKeyName.indexOf('-');
        sDeviceID = controllerKeyName.substring(0, index);
        DeviceData vpDD = vpDevServ.getDeviceData(sDeviceID);
        if (vpDD.getEmulationMode() == DBConstants.FULLEMU) {
            izEmulation = true;
        }
        // init msg handler
        setStandardInboundMessageProcessor();

    }

    /*--------------------------------------------------------------------------*/
    // Process System Inter-Process-Communication Message.
    // KR: To handle the message received from BCSPLCDevices and send it to PLC
    /*--------------------------------------------------------------------------*/
    @Override
    protected void processIPCReceivedMessage() {
        super.processIPCReceivedMessage();

        if (!receivedMessageProcessed) {
            receivedMessageProcessed = true;
            // String s = this.controllersKeyName;
            // logger.logError("BCSDevice.K2 - " + s );
            switch (receivedEventType) {
            // KR: this where we send the message to PLC eg: Stored Order
            case MessageEventConsts.CUSTOM_ALLOCATION_EVENT_TYPE:
                processCustomEventData();
                break;

            default:
                receivedMessageProcessed = false;
            }
        }
    }

    @Override
    public void startup() {
        super.startup();
        mnKeepAliveInterval = NO_ACTIVITY_TIME; // KR: the default is 15 sec in the base class which should be okay?
        mnMaxSequenceNumber = BCS_MAX_SEQNO; // per PLC
    }

    @Override
    protected void startupProtocol() {

        mnKeepAliveInterval = 0;
        logger.logDebug("startupProtocol() - Start");
        logger.logDebug("BCSDevcePort/BlockSendReceivePort.startupProtocol() - Start");
        receiveCycleTimeout = new ReceiveCycleTimeout();

        beginReceiveCycle = new BeginReceiveCycle();
        getStxOfMessage = new GetStxOfMessage();
        getFixedLengthMessage = new GetFixedLengthMessage();
        getEndDelimitedMessage = new GetEndDelimitedMessage();
        getEtxOfMessage = new GetEtxOfMessage();

        if (mnPLCReceivedKeepAliveInterval > 0) {
            if (mpPLCReceivedKeepAliveTimeout == null) {
                mpPLCReceivedKeepAliveTimeout = new PLCReceivedKeepAliveTimeout();
            }
            timers.setSSTimerEvent(mpPLCReceivedKeepAliveTimeout, mnPLCReceivedKeepAliveInterval);
            logger.logDebug("startupProtocol() -- Starting \"Keep-Alive\"");
        }

        logger.logDebug("startupProtocol() - End");
        lastRecvdMessageDateTime.setStartDateTime();

        // MCM, EBS on connection restart sequence #'s
        mnTransmittedSequenceNumber = 0;
        mnExpectedReceivedSequenceNumber = 0;
        logger.logError("BCSDevicePort.startupProtocol() - Reseting Comms for Device: " + sDeviceID
                + ", TransmittedSeq# = ExpectedReceivedSeq# = 0");

        fixedBlockLength = this.headerLength;

        setupReceiveCycle();
        logger.logDebug("BCSDevicePort/BlockSendReceivePort.startupProtocol() - End");
    }

    @Override
    protected void shutdownProtocol() {
        super.shutdownProtocol();
        logger.logDebug("shutdownProtocol() - Start");
        if (mpNoActivityTimeout != null) {
            timers.cancel(mpNoActivityTimeout);
            mpNoActivityTimeout = null;
        }
        if (mpPLCReceivedKeepAliveTimeout != null) {
            timers.cancel(mpPLCReceivedKeepAliveTimeout);
            mpPLCReceivedKeepAliveTimeout = null;
        }
        logger.logDebug("shutdownProtocol() - End");
    }

    /**
     * Process data received form port
     */
    protected void processReceivedDataBlock() {

        if (fixedBlockLength == receivedByteCount) {
            // KR : if received data and port status is running ...
            if (this.controllerStatus == ControllerConsts.STATUS_RUNNING) {
                lastRecvdMessageDateTime.setStartDateTime();
            }
            logger.logDebug("processReceivedDataBlock() -- receivedByteCount = " + receivedByteCount + ">>>"
                    + inputProtocolByteBuffer + " - " + this.controllersKeyName);

            if (verifyReceivedData()) {
                // Do not send the response to the keep alive to the device
                if (receivedDataString != null && !receivedDataString.isEmpty()) {
                    publishEquipmentEvent(receivedDataString, 0);
                }
            }
            setupReceiveCycle();
        } else {
            // TODO
            // error - reconnect
        }
    }

    /*--------------------------------------------------------------------------*/
    // Setup our Block Protocol to Start a Receive Cycle.
    /*--------------------------------------------------------------------------*/
    protected void setupReceiveCycle() {
        // logger.logDebug("setupReceiveCycle()");
        receiveCycleActive = false;
        removeAllDataMatchEvents();
        receivedByteCount = 0; // Show we didn't receive anything.
        inputProtocolSink = 0;
        if ((receiveCycleTime > 0) && (receiveCycleTimeout != null)) {
            timers.cancel(receiveCycleTimeout);
        }
        //
        // Just get the FIRST byte.
        //
        setFixedBlockLength(this.headerLength);
        if (beginReceiveCycle == null) {
            beginReceiveCycle = new BeginReceiveCycle();
        }
        setDataAvailableEvent(beginReceiveCycle, this.headerLength);
        enableEventProcessing(); // We need to explicitly enable data match checking

        lastRecvdMessageDateTime.setStartDateTime();
    }

    /**
     * TODO : This method must be implemented to return value from DB / Data store. Returns only true for now/
     * 
     * Returns AirFlow WCS is configured to treat message as Wrapped (means starts with STX, ends with ETX).
     * 
     * This method should return false in cases below.
     * 
     * <ol>
     * <li>Configured to return false</li>
     * <li>Not configured</li>
     * </ol>
     * 
     * @return true : Yes / false : No
     */
    private boolean isMessageWrapped() {
        return true;
    }

    /*--------------------------------------------------------------------------
    // Data is available from the ComPort.
    //
    // inputByteBuffer   - the received data.
    // inputBufferSink   - the offset to where the ComPort will put data.
    // inputBufferSource - the offset to the start of the Unprocessed data that
    //                     this Port needs to process.
    --------------------------------------------------------------------------*/
    @Override
    public void processAvailableData() {
        logger.logDebug("processAvailableData() -- (from ComPort)");

        if (dataWantedCount == 0) {
            if (getBlockNeedsData) {
                getBlockNeedsData = false;
            }
            return; // Nobody wants any data - ok to exit
        }
        if (rxDataCount >= dataWantedCount) {
            //
            // We have (at least) the amount of data the user wants. Copy the
            // Data Wanted Count into the Protocol's input buffer.
            //
            System.arraycopy(inputByteBuffer, inputBufferSource, inputProtocolByteBuffer, inputProtocolSink,
                    dataWantedCount);
            //
            // Update "rxDataCount" to show we have given the Protocol "dataWantedCount".
            // Also, update "inputBufferSource" & "inputProtocolSink" to show the same.
            //
            rxDataCount -= dataWantedCount;
            if (rxDataCount == 0) {
                inputBufferSink = 0; // OK to reset these
                inputBufferSource = 0;
            } else {
                inputBufferSource += dataWantedCount;
                inputProtocolSink += dataWantedCount;
            }
            //
            // Make a local copy of "DataWantedCount" in case the "PortOnDataAvailable"
            // event handler calls "SetDataAvailable" and updates it while the event
            // handler is using the reference parameter.
            //
            int tmpDataWantedCount = dataWantedCount;
            // MCM, Ikea
            // all messages are fixed length so don't reset this
            // dataWantedCount = 0; // User will update this to get more data
            if (getBlockNeedsData) {
                getBlockNeedsData = false;
            } else {
                dataAvailableEvent(tmpDataWantedCount);
            }
        }
    }

    /*
     * handles message received from PLC ports
     */
    @Override
    protected boolean verifyReceivedData() {
        return decodeReceivedData();
    }

    /**
     * Method verifies the current message type. If it Keep Alive message then it process the message and send the
     * response to the PLC. Remaining all PLC messages are decoded and converted to comma separated string for further
     * processing.
     * 
     * @return
     */
    public boolean decodeReceivedData() {
        boolean bResult = false;
        // StringBuilder receivedDataStringBuilder = new StringBuilder();

        if (receivedByteCount <= 0) {
            logger.logDebug(
                    "verifyReceivedData() -- receivedByteCount = " + receivedByteCount + "  <<<-----------------<<<");
            return bResult;
        }
        try {
            System.arraycopy(inputProtocolByteBuffer, 0, receivedByteBuffer, 0, receivedByteCount);
            if (mpBCSMessageHeader.getMsgType() == PLCConstants.PLC_LINK_STARTUP_MSG_TYPE_INT) {
                // TODOO : handle this message.
                bResult = true;
            } else if (mpBCSMessageHeader.getMsgType() == PLCConstants.KEEPALIVE_MSG_TYPE) {
                // Decode Keep alive message received from PLC and send Keep alive response
                // message back to PLC
                processKeepAliveMessage();
                bResult = true;
            } else {
                // TODO : Need refactoring
                // As multiple places re-read data from incoming buffer from PLC, it is hard to fix
                // this class to work properly. At this timing, as there is a very strict time constraint,
                // I do quick and dirty fix here, but incoming message should be wrapped to an instance at
                // only 1 place, and should not have special logic for Keep Alive (it makes code confusing).
                if (isMessageWrapped()) {
                    receivedByteBuffer = removeSTXETX(receivedByteBuffer, receivedByteCount);
                }
                setStandardInboundMessageProcessor();
                // decode the other type of PLC messages
                receivedDataString = mpMessageProcessor.decodeReceivedData(receivedByteBuffer, mpBCSMessageHeader);

                bResult = (!receivedDataString.isBlank());

            }
        } catch (Exception ex) {
            // Don't throw any exception to cutoff the connection
            logger.logException(sDeviceID, ex);
        }

        return bResult;
    }

    /**
     * Remove STX and ETX from the incoming message. This method does below.
     * 
     * When buffer[0] == STX && buffer[receivedLength - 1] == ETX, then returns data of between STX and ETX or just
     * return the given buffer.
     * 
     * 
     * @param buffer
     * @param receivedLength
     * @return
     */
    protected byte[] removeSTXETX(byte[] buffer, int receivedLength) {
        if (buffer[0] == PortConsts.STX && buffer[receivedLength - 1] == PortConsts.ETX) {
            byte[] newBuffer = new byte[buffer.length];
            System.arraycopy(buffer, 1, newBuffer, 0, receivedLength - LENGTH_OF_DELIMITER);
            return newBuffer;
        } else {
            return buffer;
        }
    }
    
    /**
     * This method send the Keep alive response message to the PLC device.
     */
    public void processKeepAliveMessage() {
        int equipmentId = getEquipmentIdFromHeader(receivedByteBuffer, isMessageWrapped());

        // Send Keep-alive response
        byte[] mabKeepAliveResponse = mpMessageProcessor.createPLCKeepAliveResponseMessage(equipmentId);
        if (isMessageWrapped()) {
            mabKeepAliveResponse = wrap(mabKeepAliveResponse);
        }

        setStandardInboundMessageProcessor();

        transmitData(mabKeepAliveResponse);
        receivedDataString = "";
        // KR: update the startDateTime
        lastRecvdMessageDateTime.setStartDateTime();
        // KR: update a record in SYSTEMHB
        updateDeviceHearthBeat(this.controllersKeyName);
    }

    /**
     * Wrap the given byte array with STX and ETX.
     * 
     * @param source byte array to be wrapped
     * @return wrapped byte array
     */
    byte[] wrap(byte[] source) {
        ByteBuffer buffer = ByteBuffer.allocate(source.length + 2);
        buffer.put(PortConsts.STX);
        buffer.put(source);
        buffer.put(PortConsts.ETX);
        return buffer.array();
    }

    /**
     * This method send the data to the PLC device
     * 
     * @param baData
     */
    private void transmitData(byte[] baData) {
        // check if port open:
        if (controllerStatus != ControllerConsts.STATUS_RUNNING) {
            return;
        }
        if (baData == null) {
            return;
        }
        int byteArrayLen = baData.length;
        putBlock(baData, byteArrayLen);
    }

    public void beginReceiveCycle_dataAvailableEvent(int count) {
        // logger.logDebug("Count: " + count + " - BeginReceiveCycle()");
        //
        // Setup to receive the rest of the message based on the protocol type.
        //
        switch (blockProtocolType) {
        case PortConsts.VARIABLE_LENGTH_BLOCK_PROTOCOL:
            //
            // The "SendResponseTimer" will deal with this
            //
            break;
        case PortConsts.FIXED_LENGTH_BLOCK_PROTOCOL: {
            //
            // Fixed length -- we know exactly how many bytes to expect (and we
            // now have the first byte). Get the rest of the message.
            //
            receivedByteCount = count;
            if (fixedBlockLength != count) {
                setDataAvailableEvent(getFixedLengthMessage, fixedBlockLength - count + (isMessageWrapped() ? 1 : 0));
            } else {
                //
                // We have all the data we need - we're done.
                //
                if (fixedBlockLength == this.headerLength) {
                    PLCMessageHeader vpBCSMessageHeader = new PLCMessageHeader();
                    setStandardInboundMessageProcessor();
                    // parse header and determine full size of message
                    vpBCSMessageHeader = mpMessageProcessor.processReceivedPLCHeader(inputProtocolByteBuffer,
                            receivedByteCount);
                    mpBCSMessageHeader = vpBCSMessageHeader;// update local

                    if (vpBCSMessageHeader == null) {
                        // an error in parsing the header occurred
                        // disconnect and reconnect
                        setControllerStatus(ControllerConsts.STATUS_ERROR);
                        logger.logOperation(LogConsts.OPR_DEVICE, "Comms Error - Shutting Down Port");
                        shutdownProtocol();
                        shutdownComPort();
                        logger.logDebug("Restart after Comms Error - startupComPort()");
                        setControllerStatus(ControllerConsts.STATUS_STARTING);
                        startupComPort();

                        fixedBlockLength = this.headerLength;
                    } else {
                        fixedBlockLength = vpBCSMessageHeader.getMsgLength() + (isMessageWrapped() ? 2 : 0);
                        setDataAvailableEvent(getFixedLengthMessage, fixedBlockLength - count);
                        processAvailableData();
                    }

                } else {
                    processReceivedDataBlock();
                    fixedBlockLength = this.headerLength;
                }
            }
        }
            break;
        case PortConsts.START_DELIMITED_BLOCK_PROTOCOL:
        case PortConsts.START_END_DELIMITED_BLOCK_PROTOCOL:
        case PortConsts.START_DELIMITED_WITH_LENGTH_BLOCK_PROTOCOL: {
            //
            // We (MAY) have the Start Delimiter for the message - see how to get the
            // rest of the message.
            //
            if (inputProtocolByteBuffer[0] == startDelimiter[0]) {
                if (protocolStripsAddsDelimiters) {
                    //
                    // Discard any garbage before the Start Delimiter (and discard the
                    // Start Delimiter, too).
                    //
                    receivedByteCount = 0;
                    inputProtocolSink = 0;
                }
                getStartOfMessage();
            } else {
                seeIfKeepAlive();
                setupReceiveCycle();
            }
        }
            break;
        case PortConsts.END_DELIMITED_BLOCK_PROTOCOL: {
            //
            // End Delimited -- just wait for the terminator.
            //
            receivedByteCount = count;
        }
            break;
        case PortConsts.PORT_SPECIFIC_BLOCK_PROTOCOL: {
            getReceivedData(count);
        }
            break;
        }
    }

    /**
     * Set the length of the sequence number
     * 
     * @param inBytes (valid range is 1 - 6)
     */
    protected void setSequenceLength(int inBytes) {

        if (inBytes < 1 || inBytes > 6) {
            throw new IllegalArgumentException("Valid range is 1 - 6 (" + inBytes + ")");
        }
        mnSequenceNumberLength = inBytes;
        mnMaxSequenceNumber = BCS_MAX_SEQNO;
    }

    /**
     * Factory for ControllerImplFactory.
     * 
     * <p>
     * <b>Details:</b> <code>create</code> is a factory method used exclusively by <code>ControllerImplFactory</code>.
     * Configurable properties of a new controller created using this method are initialized using data in the supplied
     * properties object. If the controller cannot be created, a <code>ControllerCreationException</code> is thrown.
     * </p>
     * 
     * @param ipConfig configurable property definitions
     * @return the created controller
     * @throws ControllerCreationException if an error occurred while creating the controller
     */
    public static Controller create(ReadOnlyProperties ipConfig) throws ControllerCreationException {

        return new PLCPort();
    }

    private void updateDeviceHearthBeat(String portName) {
        // KR: update the Device record dt in [SYSTEMHB] for SCADA
        synchronized (lock) {
            try {
                initializeSysHBServer();

                mpSysHBServer.updateSystemHBTime(portName);

            } catch (DBException e) {
                logger.logError("updateDeviceHearthBeat() -- Failed to update for :" + portName);
            }
        }
    }

    /*
     * ini SystemHBServer to update dt heartbeat for Devices
     */
    protected void initializeSysHBServer() {
        if (mpSysHBServer == null) {
            mpSysHBServer = Factory.create(SystemHBServer.class);
        }
    }

    /** classes **/
    /**
     * Reset the port if we don't receive any messages for the time specified by mnBCSReceivedKeepAliveInterval
     */
    public class PLCReceivedKeepAliveTimeout extends RestartableTimerTask {
        public void run() {
            BCSreceivedKeepAliveTimeout_run();
        }
    }

    /**
     * receivedKeepAliveTimeout_run -- the ReceivedKeepAliveTimeout's run() needs to be synchronized so that any work we
     * do here is not interrupted by any incoming messages or events that we generate here. We want to complete anything
     * we do here without being preempted.
     */
    private void BCSreceivedKeepAliveTimeout_run() {
        synchronized (mpLock) {
            mpLock = 2;
            // long interval = lastRecvdMessageDateTime.getElapsedDateTimeAsLong();
            if (lastRecvdMessageDateTime.getElapsedDateTimeAsLong() > EBS_NO_ACTIVITY_TIMEOUT)
            // if( false )
            {
                if (!ShuttingDownStarted) {

                    // KR:set to true to avoid calling again
                    ShuttingDownStarted = true;// to avoid calling this again

                    // If we have not received a message in X # of seconds
                    // drop and reconnect
                    setControllerStatus(ControllerConsts.STATUS_ERROR);
                    logger.logOperation(LogConsts.OPR_DEVICE,
                            "ReceivedKeepAliveTimeout - Shutting Down Port" + this.controllersKeyName);
                    shutdownProtocol();
                    shutdownComPort();
                    logger.logDebug("ReceivedKeepAliveTimeout - startupComPort()" + this.controllersKeyName);
                    setControllerStatus(ControllerConsts.STATUS_STARTING);
                    startupComPort();
                    fixedBlockLength = this.headerLength;
                    // KR: set to false to expect calling after no activity period again
                    lastRecvdMessageDateTime.setStartDateTime();
                    ShuttingDownStarted = false;
                }
            } else {
                if (mnPLCReceivedKeepAliveInterval > 0) {
                    timers.setSSTimerEvent(mpPLCReceivedKeepAliveTimeout, mnPLCReceivedKeepAliveInterval);
                    logger.logDebug("startupProtocol() -- Starting \"Keep-Alive\"");
                }
            }
        }
    }

    /**
     * Process data received from BCSPLCDevice which need to be sent to PLC
     */
    private void processCustomEventData() {
        String data = super.receivedText;// global value which contains the data received from SENDER

        logger.logDebug("processCustomEventData received: " + data);
        if (data.isEmpty()) {
            return;
        }
        // conditionDataToTransmit converts the string to byte[] based on the msg type
        // which is the first CHAR of the string
        byte[] baData = conditionDataToTransmit(data);
        if (baData != null) {
            // send it to PORT
            if (isMessageWrapped()) {
                baData = wrap(baData);
            }
            transmitData(baData);
        }
    }
    /* ========================================================================== */

    /**
     * Convert the string data to byte[] before sending to target PORT (i.e PLC)
     */
    @Override
    protected byte[] conditionDataToTransmit(String sData) {
        setStandardMessageEncoderImpl();
        return mpStandardMessageEncoderImpl.encode(sData);
    }

    /**
     * Extracts equipment ID from the given header buffer. This method is protected for testing purpose.
     * 
     * @param receivedBuffer Header data
     * @return Equipment ID
     */
    protected Integer getEquipmentIdFromHeader(byte[] receivedBuffer, boolean isWrapped) {
        Integer equipmentId = 0;
        byte[] equipmentIdByte = new byte[PLCConstants.MSG_DWORD_LEN];

        System.arraycopy(receivedBuffer, PLCConstants.PLC_HEADER_OFFSET_DEVICE_ID + (isWrapped ? 1 : 0),
                equipmentIdByte, 0, PLCConstants.MSG_DWORD_LEN);
        if (equipmentIdByte != null) {
            ByteBuffer buf = ByteBuffer.wrap(equipmentIdByte, 0, equipmentIdByte.length);
            equipmentId = buf.getInt(0);

        }
        return equipmentId;
    }

    /**************************** private methods *********************/
    private void setStandardInboundMessageProcessor() {
        if (mpMessageProcessor == null) {
            mpMessageProcessor = Factory.create(StandardMessageProcessor.class);
        }
    }

    private void setStandardMessageEncoderImpl() {
        if (mpStandardMessageEncoderImpl == null) {
            mpStandardMessageEncoderImpl = Factory.create(StandardMessageEncoderImpl.class);
        }
    }

    @Override
    protected void startupComPort() {
        logger.logDebug(getClass().getSimpleName() + " -- comPort.startup()");
        // Startup this ComDevice's actual ComPort (communication physical layer).
        logger.logDebug(getClass().getSimpleName() + " -- startup() -- inputByteBufferSize: "
                + PortConsts.COM_DEVICE_INPUT_BUFFER_SIZE + " -- outputByteBufferSize: "
                + PortConsts.COM_DEVICE_OUTPUT_BUFFER_SIZE);

        comPort = new CustomSocketComPort();
        if (comPort != null) {
            comPort.setProperties(mpProperties);
            comPort.initialize(logger);
            comPort.setComPortEventHandler(this);
            comPort.startup();
        } else {
            logger.logError("ComPort NOT Started - startupComPort()");
        }
    }

}
