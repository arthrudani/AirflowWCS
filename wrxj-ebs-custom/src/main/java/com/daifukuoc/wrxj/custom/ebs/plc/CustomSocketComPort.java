package com.daifukuoc.wrxj.custom.ebs.plc;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.daifukuamerica.wrxj.comport.ComPortImpl;
import com.daifukuamerica.wrxj.comport.net.SocketComPort;

public class CustomSocketComPort extends SocketComPort {
	public CustomSocketComPort() {
		super();
	}

	@Override
	protected void acceptConnection() {
		// Socket used for establishing a connection
		Socket clientSock = null;
		logAllDebug("Listen/ServerSocket WAITING to Accept Connection -- Name \"" + connectToHostName + "\"  IPAddr \""
				+ connectToHostIPAddress + "\"  Port: " + connectToHostPortNumber);
		try {
			//
			// Now, we must BLOCK/WAIT to Accept a Client connection - BLOCKING I/O!
			//
			clientSock = connectedServerSocket.accept();
			clientSock.setSoTimeout(1); // Set the time out to the minimum
			//
			// If Accept returned without error, then we accepted Client Socket
			// connection.
			//
			inetAddress = clientSock.getInetAddress(); // Convert host into an InetAddress.
			hostName = inetAddress.getHostName();
			hostIPAddress = inetAddress.getHostAddress();
			hostPortNumber = clientSock.getPort();
			logAllDebug("Accepted Connection from Host: Name \"" + hostName + "\"  IPAddr \"" + hostIPAddress
					+ "\"  Port: " + hostPortNumber);
			connectedClientSocket = clientSock;
			synchronized (this) {
				notifyAll();
			}
		} catch (IOException ioe) {
			/*
			 * Typically, the listen socket was blocking/waiting to accept a connection when
			 * we closed the socket (during shutdown).
			 */
			logSocketDebug("Waiting to Accept Connection -- IOException: " + ioe.toString());
		}
	}

	public class CustomControlThread extends ComPortImpl.ControlThread {

		/**
		 * ControlThread - ctInputAvailableData
		 */
		@Override
		protected void ctInputAvailableData() {
			try {
				synchronized (inputLock) {
					int bufferFreeCount = COM_PORT_INPUT_BUFFER_SIZE - inputBufferSink;
					if (bufferFreeCount > 0) {
						try {
							int bytesRead = inputStream.read(inputByteBuffer, inputBufferSink, bufferFreeCount);
							//
							// We can be told that bytes are available, but when we read there is
							// no data there - so check if we actually read anything.
							//
							if (bytesRead > 0) {
								logger.logRxByteCommunication(inputByteBuffer, inputBufferSink, bytesRead);
								inputBufferSink += bytesRead;
								if (inputBufferSink > COM_PORT_INPUT_BUFFER_SIZE) {
									logError("ctInputAvailableData() -- Rx Buffer OVERFLOW -- bufferFreeCount:"
											+ bufferFreeCount);
									inputBufferSink = COM_PORT_INPUT_BUFFER_SIZE;
								}
							}

							else if (bytesRead == -1) {
								comPortState = COM_PORT_STATE_CONNECTING;
							}

						} catch (SocketTimeoutException ex) {
							logger.logDebug("ctInputAvailableData() - SocketTimeOutException: " + ex.toString());
							logger.logDebug("ctInputAvailableData() - Take it not an error, proceed to next reading");

						} catch (SocketException ex) {
							logger.logDebug("ctInputAvailableData() - SocketException: " + ex.toString());
							logger.logDebug("ctInputAvailableData() - Wait for reconnection from client");
							comPortState = COM_PORT_STATE_CONNECTING;
						}
					} else {
						logger.logDebug(
								"ctInputAvailableData() -- Rx Buffer FULL -- bufferFreeCount:" + bufferFreeCount);
					}
				} // synchronized

			} catch (IOException ioe) {
				logger.logDebug("ctInputAvailableData() - inputStream.available() -- IOException: " + ioe.toString());
				//
				// Not much we can do here except kick it upstairs.
				//
				comPortState = COM_PORT_STATE_ERROR;
			}
		}
	}

	@Override
	public void run() {
		while (!quit) {
			int tmpComPortState = COM_PORT_STATE_UNKNOWN;
			while (tmpComPortState != comPortState) {
				tmpComPortState = comPortState; // update for next pass.
				switch (comPortState) {
				case COM_PORT_STATE_ERROR:
					;
					break;
				case COM_PORT_STATE_UNKNOWN:
					;
					break;
				case COM_PORT_STATE_CREATE: {
					comPortState = COM_PORT_STATE_CREATING;
					createComPortControlThread();
				}
					break;
				case COM_PORT_STATE_CREATING: {
					synchronized (creationLock) {
						if (isCreated()) {
							comPortState = COM_PORT_STATE_CREATED;
						}
					}
				}
					break;
				case COM_PORT_STATE_CREATED: {
					synchronized (creationLock) {
						updateCreatedState();
					}
				}
					break;
				case COM_PORT_STATE_CONNECT: {
					//
					// We need to have our ServerSocket ACCEPT a Connection.
					//
					synchronized (creationLock) {
						comPortState = COM_PORT_STATE_CONNECTING;
						notifyControlThread();
					}
				}
					break;
				case COM_PORT_STATE_CONNECTING: {
					synchronized (creationLock) {
						if (isConnected()) {
							comPortState = COM_PORT_STATE_CONNECTED;
						}
					}
				}
					break;
				case COM_PORT_STATE_CONNECTED: {
					//
					// We have a Connection established to our ClientSocket.
					// We need to attach Input/Output DataStreams.
					//
					attachIOStreams();

					comPortState = COM_PORT_STATE_RUNNING;
				}
					break;
				case COM_PORT_STATE_RUNNING: {
					if (inputBufferSource != inputBufferSink) {
						dataIsAvailable();
					}
					notifyControlThread();
				}
					break;
				case COM_PORT_STATE_STOP:
					;
					break;
				case COM_PORT_STATE_STOPPING:
					;
					break;
				case COM_PORT_STATE_STOPPED:
					;
					break;
				default:
					logError("UNKNOWN comPortState: " + comPortState);
					break;
				}
				if (comPortState != previousComPortState) {
					processComPortStateChange();
				}
			}
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException e) {
			}
		}
		if (logger != null) {
			logger.logDebug("run2() -- DONE - call closeComPort() and EXIT");
		}
		closeComPort();
		logger = null;
	}

	@Override
	protected void createComPortControlThread() {
		if (controlThread == null) {
			logger.logDebug("createComPortControlThread -- Start");

			// Create a new Control THREAD (which will then create the actual Socket).
			logger.logDebug("createComPortControlThread -- Creating ControlThread");
			controlThread = new CustomControlThread();
			controlThread.setName("ComPortImpl-ControlThread-" + logger.getLoggerInstanceName());
			logger.logDebug("createComPortControlThread -- Starting ControlThread");

			/*
			 * Start the ControlThread running. "run()" will CREATE the socket and, if it is
			 * a Client Socket, CONNECT it (and BLOCK while connecting!).
			 */
			controlThread.start();
			logger.logDebug("createComPortControlThread -- End");
		}
	}
}
