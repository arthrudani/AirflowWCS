using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Text;
using System.Windows.Forms;
using PostcodeGUI.TCP;
using PostcodeGUI.Tools;
using System.Threading;

namespace PostcodeGUI
{
  	

	public struct MessageInfo
	{
		public	ushort MessageID;
		public	ushort SubmessageID;

		public MessageInfo( ushort id , ushort subId )
		{
			MessageID = id;
			SubmessageID = subId;
		}
	}


    public class Message : IMessage
    {
        
   
		List<BaseParameter>   _Parameters;

        public byte MessageID
        {
            get
            {
				return ((MessageHeader)_Header).MessageID;
            }
        }
  
        public byte SubMessageID
        {
            get
            {
                return ((MessageHeader)_Header).MessageSubID;
            }
        }

        public List<BaseParameter> Parameters
        {
            get
            {
				return _Parameters;
            }
        }

		public int Length
		{
			get
			{
				return _Header.MessageLength - _Header.HeaderLength;
			}
		}

		/*
		public Message( MessageIDS id , MessageSubIDS subID , Mutex writeLock )
			: this( id , subID , ParamSet.TypeUser , writeLock )
		{

		}
		*/

		public Message( MessageIDS id , MessageSubIDS subID , Messagetype type ,  Mutex writeLock )
		{
			base._Header = new MessageHeader( id , subID , type );
			
			_Parameters = new List<BaseParameter>();
			
			_WriteLock = writeLock;
		}
   		

		public int SetLength( )
		{
			_Header.MessageLength = 0;
			foreach( BaseParameter parm in Parameters )
			{
				_Header.MessageLength += parm.Size;
			}
			_Header.MessageLength+=_Header.HeaderLength;
			return _Header.HeaderLength;

		}

		public override bool Finalize( )
		{
			SetLength();
			return true;
		}

		
        public void SetDataInvalid()
        {
			foreach( BaseParameter parm in Parameters )
			{
				parm.DataValid = false;
			}   
        }

		
 
        public void AddParameter(BaseParameter param)
        {
            _Parameters.Add(param);
        }

		public void AddParameter( BaseParameter param , int index )
		{
			if( Parameters.Count != index )
			{
				throw new Exception( "Parameter can't be added to specified index" );
			}
			AddParameter( param );
 		}

			/*
        public void GetParameterSet(StringCollection parmStr)
        {
            foreach (BaseParameter param in Parameters)
            {
                parmStr.Add(param.ToString());
            }
        }
			*/
		/*	 
        public byte[] GetMessageParameters()
        {
			byte[] buffer = new byte[ _Length ];
			int offset = 0;
            foreach (BaseParameter param in Parameters)
            {
                param.WriteToByteArray(buffer, ref offset);
            }
			return buffer;
	    }
		*/
/*	
		public void ParseMessage(Byte[] buffer, StringCollection text , ref int offset )
		{
			foreach (BaseParameter param in Parameters)
			{
				text.Add(param.Parse(buffer,ref offset));
			}
		}
*/

		public static Message Create( MessageHeader hdr )
		{
			return Message.Create( (MessageIDS)hdr.MessageID , (MessageSubIDS)hdr.MessageSubID , (Messagetype)hdr.Type , null );
		}

		public static Message Create( MessageIDS id, MessageSubIDS subID, Messagetype type , Mutex writeLock  )
		{
			Message msg = new Message( id , subID ,type , writeLock );
			switch( type )
			{
				case Messagetype.TypeStatusRequest:
				case Messagetype.HeaderOnly :
					break;
				case Messagetype.TypePLCStatus:
					msg.Parameters.Add( new ByteParam( "id" ) );
					msg.Parameters.Add( new ByteParam( "Status" ) ); 
					break;
				case Messagetype.TypeShiftStatus:
				case Messagetype.TypePostCodeTablestatus:
					msg.Parameters.Add( new ByteParam ( "Status" )); 
					break;
				case Messagetype.TypeTerminalStatus:
					msg.AddParameter( new ByteParam( "ID" ) );		
					msg.AddParameter( new ByteParam( "Status" ) );			
					msg.AddParameter( new DWordParam( "Count" ) );		
					break;
				case Messagetype.TypeBuffer: 
					msg.Parameters.Add( new ByteBuffer( "Buffer" , 20 ) ); // 20 is initial size only parameter is variable length
					break;
				case Messagetype.TypeEvent:
					msg.AddParameter( new WordParam( "Event ID" ) );
					msg.AddParameter( new DateTimeParam( "Time Stamp" ) );
					msg.AddParameter( new ByteParam( "Sevierity" ) );
					msg.AddParameter( new ByteString( "Equipmrny" , 8 ) );
					msg.AddParameter( new ByteString( "Message" , 8 ) );
					((DateTimeParam)msg.Parameters[ 1 ]).Format = MainPostCodeGui.DateFormat ;
					break;
				case Messagetype.TypeTransaction :
					msg.AddParameter( new WordParam( "Trans ID" ) );
					msg.AddParameter( new DateTimeParam( "Time Stamp" ) );
					msg.AddParameter( new ByteString( "Equipment" , 8 ) );
					msg.AddParameter( new ByteString( "Post Code" , 8 ) );
					msg.AddParameter( new WordParam ( "Run"  ) );
					( ( DateTimeParam )msg.Parameters[ 1 ] ).Format = MainPostCodeGui.DateFormat;
					//msg.AddParameter( new ByteString( "Message" , 8 ) );
					break;
				case Messagetype.EventRequest:
					msg.AddParameter( new WordParam( "No-id" ));
					break;
				case Messagetype.LogRequest :
					msg.AddParameter( new ByteParam( "Svr" ) );	
					msg.AddParameter( new ByteString( "Text" , 8 ) );
					break;
					
			}
 			return msg;
		}
  
		public override int Read(  byte[] buffer , ref int offset )
		{
			int start = offset;
			foreach( BaseParameter param in Parameters )
			{
				param.ReadFromByteArray( buffer , ref offset );
			}
			Finalize();
			return offset - start;;
		}

		public override int Write( byte[] buffer )
		{
			int offset = 0;
			foreach( BaseParameter param in Parameters )
			{
				param.WriteToByteArray( buffer , ref offset );
			}
			return offset;
		}
    }
}



