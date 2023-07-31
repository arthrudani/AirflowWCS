using System;
using System.Collections.Generic;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Windows.Forms;
using System.IO;
using AirflowDeviceSim.TCP;
using System.Configuration;



namespace AirflowDeviceSim.TCP
{
	public enum TCPConnectionState
	{
		Disconnected ,
		Connecting ,
		Connected ,
		Disconnecting ,
	}


	public delegate void TCPConnectionEvent( TCPConnectionState state  );
    public delegate void MessageReceivedEvent( Object _sender );
    public delegate void MessageSentEvent(Message _message);
 

	public class TCPClientInterface	: IDisposable
	{
		private  bool Disposed;
		
		private bool LastConnected;
		protected TcpClient		TCPConnection;
		private Thread			CommThread;
		private BinaryReader	Reader;
		private BinaryWriter	Writer;
	
		private Mutex _DispatchLock;
		private TCPConnectionEvent _StatusCallback;
        public MessageReceivedEvent MessageReceived;
        public MessageSentEvent MessageSent;
        public Action<Exception> ReportError;
		private MessageQueue	_OutQ;
        private MessageQueue _InQ;

		private IMessageFactory Factory;
	   

		//private List<MessageDispatch  > _DispatchList;

		private byte[] ReadBuffer;
		private byte[] WriteBuffer;
		private byte[] WriteTotalBuffer;

		private byte[] ReadHeader;
		private byte[] WriteHeader;

		protected IPAddress ServerIP;
		protected int ServerPort;

        //KR added new var to handle STX and ETX
        int ReceivingHeaderLen = 0;

		//private IMessage LastFailedToSend;
		//private int ConnectionRetrys;
        private bool Terminating;     

        

        public IPAddress GetServerIP
        {
            get
            {
                return ServerIP;
            }
        }
		
		public TCPConnectionEvent StatusCallback
		{
			set
			{
				_StatusCallback = value;
			}
		}
		
		//This is true if we receive messages from server
		public bool Connected
		{
			get
			{
                if (TCPConnection == null)
                {
                    return false;
                }

				try
				{
					return TCPConnection.Connected;
				}
				catch
				{
					return false;
				}
				
			}

			set
			{
				LastConnected = value;
				_StatusCallback( value == true ? TCPConnectionState.Connected : TCPConnectionState.Disconnected );  
			}
		}

        public int SendTimeout
        {
            get;
            set;
        }

        public int ReceiveTimeout
        {
            get;
            set;
        }

        public bool NetworkByteOrder
        {
            get;
            set;
        }
        //KR: to include STX and ETX to the message. Default is false;
        public bool AddWrapper
        {
            get;
            set;
        }
		public TCPClientInterface( IPAddress serverIP, int port , IMessageFactory _factory )
		{
			_StatusCallback = this.OnStateChanged;
			Connected = false;
            SendTimeout = 1000;
            ReceiveTimeout = 200;
			_DispatchLock = new Mutex();
			
			ServerIP = serverIP;
			ServerPort = port;
			ReadBuffer = new byte[ 40 ];
			WriteBuffer = new byte[ 40 ];
			WriteTotalBuffer = new byte[ 40 ];
			_OutQ	= new MessageQueue();
            _InQ = new MessageQueue();

            Factory = _factory;
            if (ConfigurationManager.AppSettings["AddWrapping"] != null)
            {
                bool outBool;
                string strWrapping = ConfigurationManager.AppSettings["AddWrapping"];
                Boolean.TryParse(strWrapping, out outBool);
                AddWrapper = outBool;
            }
            
            
            ReceivingHeaderLen = (AddWrapper) ?  Factory.ReceivingHedaerLenght + 1 : Factory.ReceivingHedaerLenght ;
            ReadHeader = new byte[ReceivingHeaderLen]; //KR: only for reading
            WriteHeader = new byte[Factory.ReceivingHedaerLenght]; 

			TCPConnection = null;
			
			Reader = null;
			Writer = null;
			//LastFailedToSend = null;

			CommThread = new Thread(new ThreadStart(this.ThreadExecute));
			Disposed = false;
			
			//ConnectionRetrys = 0;
            Terminating = false;           
			_StatusCallback(TCPConnectionState.Disconnected);  
            MessageReceived = OnMessageReceived;
            MessageSent = OnMessageSent;
            ReportError = OnReportError;
		}


		private void Dispose( bool disposing )
		{
			// Check to see if Dispose has already been called.
			if( false == this.Disposed )
			{
				Stop();
			}
			Disposed = true;
		}

		public void Dispose( )
		{
			Dispose( true );
			// This object will be cleaned up by the Dispose method.
			// Therefore, you should call GC.SupressFinalize to
			// take this object off the finalization queue 
			// and prevent finalization code for this object
			// from executing a second time.
			GC.SuppressFinalize( this );
		}

        public void Send(Message _toSend)
        {
            _OutQ.Enqueue(_toSend);
        }

        void OnMessageReceived( Object _sender )
        {
            
        }

        void OnMessageSent(Message _message)
        {

        }

        void OnReportError(Exception _ex)
        {
        }

        public Message GetMessage()
        {
            return _InQ.Dequeue();

        }

		public virtual void OnStateChanged( TCPConnectionState state )
		{

		}

        //public MessageDispatch Register( IMessageProvider provider , IMessageHandler handler , List<MessageHeaderBase> messages )
        //{
        //    MessageDispatch disp = new MessageDispatch( provider ,handler , messages );
        //    _DispatchLock.WaitOne();
        //    _DispatchList.Add( disp );
        //    _DispatchLock.ReleaseMutex();
        //    return disp;
        //}

        public virtual void OnStart()
        {
            
        }

        public virtual void OnStop()
        {
            CloseConnection();
        }

		public void Start()
		{
            OnStart();
            Terminating = false;           
            CommThread.Start();            
		}


        public virtual void OnCloseConnection()
        {


        }

 

		public void Stop( )
		{
			if( null != CommThread )
			{
              
                Terminating = true;
                //CommThread.Abort();//!KB added 20070912 - end thread
                //Thread.Sleep(6000);
                CommThread.Join();
                //while (CommThread.IsAlive)
                //{
                //    Thread.Sleep(100);
                //}
                OnStop();
			}
		}

      

		public void CloseConnection( )
		{
            OnCloseConnection();
			
			if( Reader != null )
			{
				Reader.Close();
				Reader = null;
			}
			if( Writer != null )
			{
				Writer.Close();
				Writer = null;
			}
            if (TCPConnection != null)
            {
                TCPConnection.Close();
                TCPConnection = null;
            }
            Connected = false;
		}


        virtual protected bool ShouldTryConnect()
        {
            return !Connected;
        }
    


        protected virtual bool TryConnect()
        {
            if (TCPConnection == null)
            {
                TCPConnection = new TcpClient();
                //TCPConnection.ReceiveTimeout = ReceiveTimeout;
                TCPConnection.SendTimeout = SendTimeout;
            }
            TCPConnection.Connect(ServerIP, ServerPort);
            return TCPConnection.Connected;

        }

        public void InitStreams()
        {
            TCPConnection.GetStream().Flush();
            Reader = new BinaryReader(TCPConnection.GetStream());
            Writer = new BinaryWriter(TCPConnection.GetStream());
            Writer.Flush();
            while (0 < TCPConnection.Available)
            {
                Reader.ReadByte();
            }
        }

		private void ThreadExecute()
		{
            try
            {
                
                while (!Terminating)
                {

                    if (ShouldTryConnect())
                    {
                        if (LastConnected == true)
                        {
                            CloseConnection();
                        }

                        try
                        {

                            if (TryConnect())
                            {
                                InitStreams();
                                Connected = true;
                            }
                        }
                        catch (SocketException _ex)
                        {
                            ReportError(_ex);
                            //Logger.Instance.Append("TCPClientInterface::ThreadExecute: Socket Exception: " + e.ErrorCode + " " + e.Message + " at " + ServerIP.ToString());
                        }
                        catch (ObjectDisposedException _ex)
                        {
                            ReportError(_ex);
                            //Client socket closed
                            // Logger.Instance.Append("TCPClientInterface::ThreadExecute: Client socket closed at " + ServerIP.ToString());
                        }
                        catch (ArgumentOutOfRangeException _ex)
                        {
                            ReportError(_ex);
                            //port out of range
                            //Logger.Instance.Append("TCPClientInterface::ThreadExecute: Port out of range at " + ServerIP.ToString());
                        }
                        //if (TCPConnection.Connected)
                        //{
                            

                        //}
                    }

                    if (TCPConnection != null && true == TCPConnection.Connected)
                    {
                        //ConnectionRetrys = 0;

                        try
                        {
                            ReceiveMessages();
                            SendMessages();
                        }
                        catch (SocketException _ex)
                        {
                            ReportError(_ex);
                            CloseConnection();
                        }
                        catch (ObjectDisposedException _ex)
                        {
                            ReportError(_ex);
                            //Client socket closed
                            CloseConnection();
                        }
                        catch (System.IO.IOException ex)
                        {


                            // when unable to write 
                            ReportError(ex);
                            CloseConnection();
                        }
                        catch (NotSupportedException ex)
                        {
                            ReportError(ex);
                        }
                        catch (TimeoutException ex)
                        {
                            ReportError(ex);
                        }

                    }

                    Thread.Sleep(5);
                }
            }
            catch (ThreadAbortException)
            {
                CloseConnection();
            }
          

 		}

		void SendMessage( Message toSend )
		{
          
            toSend.Header.Write(WriteHeader, NetworkByteOrder);
			//try
			{
                int msgLength = toSend.Length;
                if (0 < msgLength)
				{
					Array.Resize( ref WriteBuffer , msgLength );
					toSend.Write( WriteBuffer , NetworkByteOrder );

                    Array.Resize(ref WriteTotalBuffer, toSend.Header.Length + msgLength);

					Buffer.BlockCopy( WriteHeader , 0 , WriteTotalBuffer , 0 , toSend.Header.Length );
					Buffer.BlockCopy( WriteBuffer , 0 , WriteTotalBuffer , toSend.Header.Length , msgLength );
                   
                    if (this.AddWrapper)
                    {//KR here I need to add STX and ETX
                        byte[] STXbyte = {MessageConst.STX};
                        byte[] ETXbyte = { MessageConst.ETX };
                        byte[] BufferWithWrapper = new byte[WriteTotalBuffer.Length + 2 ];
                        Buffer.BlockCopy(STXbyte , 0, BufferWithWrapper, 0, 1);
                        Buffer.BlockCopy(WriteTotalBuffer, 0, BufferWithWrapper, 1, WriteTotalBuffer.Length);
                        Buffer.BlockCopy(ETXbyte, 0, BufferWithWrapper, BufferWithWrapper.Length - 1, 1);

                        Writer.Write(BufferWithWrapper);
                    }
                    else
                    {
                        Writer.Write(WriteTotalBuffer);
                    }

                    toSend.ByteCount = WriteTotalBuffer.Length;
					//LastFailedToSend = null;
				}
				else
				{
					Writer.Write( WriteHeader );
                    toSend.ByteCount = WriteHeader.Length;
				}
			}
			/*
			catch( SocketException e )
			{
				//!KB modified 20070911
				//Logger.Instance.Append("TCPClientInterface::SendMessage(): SocketException: " + e.ErrorCode + " " + e.Message + " at " + ServerIP.ToString());
			}
			catch( ObjectDisposedException )
			{
				//Client socket closed
				//!KB modified 20070911
				//Logger.Instance.Append("TCPClientInterface::SendMessage(): ObjectDisposedException" + " at " + ServerIP.ToString());
			}
			catch( ArgumentOutOfRangeException )
			{
				//port out of range
				//!KB modified 20070911
				//Logger.Instance.Append("TCPClientInterface::SendMessage(): ArgumentOutOfRangeException" + " at " + ServerIP.ToString());
			}
			catch( System.IO.IOException e )
			{
				//!KB modified 20070911
				//Logger.Instance.Append("TCPClientInterface::SendMessage(): IO Exception" + " " + e.ToString() + " at " + ServerIP.ToString());
				// when unable to write 
				//MessageBox.Show( e.Message);
			}
			catch( Exception ex )
			{

			}
			*/ 
		}

		void SendMessages( )
		{
			Message toSend = null;

			while( null != ( toSend = _OutQ.Dequeue() ) )
			{
				SendMessage( toSend);
                toSend.Header.TimeStamp = DateTime.Now;
                MessageSent(toSend);
			}
 		}

		int ReadFromSocket( byte[] buffer , int buferIndex , int count )
		{
            DateTime start = DateTime.Now;
			int countRead = 0;
			while( countRead < count )
			{
				countRead += Reader.Read( buffer , buferIndex + countRead , count - countRead );
                if (ReceiveTimeout > 0 && (DateTime.Now - start).TotalMilliseconds > ReceiveTimeout)
                {
                    break;
                }
			}
			return countRead;
		}


        String GetByteValues(byte[] _data, int count)
        {
            String str = "";
            for (int i = 0; i < count; i++)
            {
                str += _data[i].ToString("X2") + ' ';
            }
            return str;
        }

        void ReceiveMessages()
		{
			try
			{
                
				//while( Factory.ReceivingHedaerLenght <= TCPConnection.Available )
                while ( 0 < TCPConnection.Available)
				{
                    int totalAvailable = TCPConnection.Available;
                    MessageHeader header = Factory.CreateReceivingHeader();

                    //to handle  STX and ETX
                    //Factory.ReceivingHedaerLenght replaced by ReceivingHeaderLen

                    int countRead = ReadFromSocket(ReadHeader, 0, ReceivingHeaderLen);
                    if (countRead < ReceivingHeaderLen)
                    {
                        String message = "Timeout after " + ReceiveTimeout.ToString() + " ms. while receiving message header. Expected " + ReceivingHeaderLen.ToString() + " Received " + countRead.ToString() + " [" + GetByteValues(ReadHeader, countRead) + ']';
                        throw new TimeoutException(message);
                    }

                    if (AddWrapper)
                    {
                        //should remove the STX from the begging of the header
                        byte[] rdHeader = new byte[Factory.ReceivingHedaerLenght];
                        Array.Copy(ReadHeader, 1, rdHeader, 0, Factory.ReceivingHedaerLenght);

                        header.Read(rdHeader, NetworkByteOrder);
                    }
                    else
                    {
                        header.Read(ReadHeader, NetworkByteOrder);
                    }

                    
                    header.TimeStamp = DateTime.Now;
                    Message toRead = Factory.CreateMessage(header, Direction.Receive);

                    if (toRead == null)
                    {
                        throw new NotSupportedException("No message available for header: " + header.ToString());
                    }

                    int bytesToRead = (AddWrapper) ? toRead.Length + 1 : toRead.Length;

					if( bytesToRead > 0 )
					{
                        countRead = 0;
						Array.Resize( ref ReadBuffer , bytesToRead );
						//Reader.Read( ReadBuffer , 0 , bytesToRead );
                        if ((countRead = ReadFromSocket(ReadBuffer, 0, bytesToRead)) < bytesToRead)
                        {
                            throw new TimeoutException("Timeout after " + ReceiveTimeout.ToString() + " ms. while receiving message" + header.ToString() + ". Expected " + bytesToRead.ToString() + " Received " + countRead.ToString());
                        }
                        toRead.Read(ReadBuffer,NetworkByteOrder);
                        toRead.ByteCount = ReadBuffer.Length;
  				    }
                    toRead.Header.TimeStamp = DateTime.Now;
                    toRead.ByteCount += ReadHeader.Length;
                    _InQ.Enqueue(toRead);
                    MessageReceived(this);
                    //int left = totalAvailable - (toRead.Header.Length + toRead.Length);
                    //if ( left > 0)
                   // {
                   //     ReportError( new Exception( toRead.Header.TimeStamp.ToString("HH:mm:ss.ffff") + ' ' + left.ToString() +" bytes left after reading message.")); 
                   // }
                   
			    }
              

			}
			catch( SocketException )
			{
                //!KB modified 20070911
                //Logger.Instance.Append("TCPClientInterface::ReceiveMessages(): SocketException: " + e.ErrorCode + " " + e.Message + " at " + ServerIP.ToString());
            }

			catch( ObjectDisposedException  )
			{
                //!KB modified 20070911
                //Logger.Instance.Append("TCPClientInterface::ReceiveMessages(): ObjectDisposedException" + " at " + ServerIP.ToString());
                //Client socket closed
			}
			catch( ArgumentOutOfRangeException  )
			{
                //!KB modified 20070911
                //Logger.Instance.Append("TCPClientInterface::ReceiveMessages(): ArgumentOutOfRangeException" + " at " + ServerIP.ToString());
                //port out of range
			}
		}

		

		
	}
}
