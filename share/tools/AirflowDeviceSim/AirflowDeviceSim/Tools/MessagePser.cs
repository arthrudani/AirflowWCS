using System;
using System.Collections.Generic;
using System.Text;
using System.Collections.Specialized;


namespace AirflowDeviceSim
{
	public class MessageParser
	{
		/*
		private Message m_message;
		private Message m_submessage;


		public MessageParser()
		{
			m_message = new Message( "Parser Message", 0 , ParameterSet.TypeNine, ParamName.TypeOne ); 
			m_submessage = m_message.AddSubMessage("parser", 0 );
		}
		*/
		/*
		public static short BufferToInt16(byte[] buffer, ref int offset )
		{
			byte[] temp = new Byte[ 2 ];
			Buffer.BlockCopy( buffer , offset , temp, 0 , 2 );
			offset += 2;
			short val = BitConverter.ToInt16( temp , 0 );
			return val;
		}
		*/
		public static ushort BufferToUInt16( byte[] buffer , ref int offset )
		{
			byte[] temp = new Byte[ 2 ];
			Buffer.BlockCopy( buffer , offset , temp , 0 , 2 );
			offset += 2;
			ushort val = BitConverter.ToUInt16( temp , 0 );
			return val;
		}
	   /*
		public static int BufferToInt32( byte[] buffer , ref int offset )
		{
			byte[] temp = new Byte[ 4 ];
			Buffer.BlockCopy( buffer , offset , temp , 0 , 4 );
			offset += 4;
			int val = BitConverter.ToInt32( temp , 0 );
			return val;
		}
	   */
		public static uint BufferToUInt32( byte[] buffer , ref int offset )
		{
			byte[] temp = new Byte[ 4 ];
			Buffer.BlockCopy( buffer , offset , temp , 0 , 4 );
			offset += 4;
			uint val = BitConverter.ToUInt32( temp , 0 );
			return val;
		}

		public static Int64 BufferToInt64( byte[] buffer , ref int offset )
		{
			byte[] temp = new Byte[ 8 ];
			Buffer.BlockCopy( buffer , offset , temp , 0 , 8 );
			offset += 8;
			Int64 val = BitConverter.ToInt64( temp , 0 );
			return val;
		}

		public static UInt64 BufferToUInt64( byte[] buffer , ref int offset )
		{
			byte[] temp = new Byte[ 8 ];
			Buffer.BlockCopy( buffer , offset , temp , 0 , 8 );
			offset += 8;
			UInt64 val = BitConverter.ToUInt64( temp , 0 );
			return val;
		}

		public static byte BufferToByte( byte[] buffer , ref int offset )
		{
			byte ret =  buffer[ offset ];
			offset += 1;
			return ret;
		}


		public static void UInt16ToBuffer( UInt16 data , Byte[] buffer , ref int offset )
		{
			Byte[] rempAttay = BitConverter.GetBytes( data );
			Buffer.BlockCopy( rempAttay , 0 , buffer , offset , 2 );
			offset += 2;
		}



		/*
		public bool ParseMessage( byte[] buffer, StringCollection text )
		{
			int offset = 0;
			short messageID = MessageParser.BufferToInt16( buffer , ref offset );  
			text.Add( messageID.ToString());
			short subID = MessageParser.BufferToInt16( buffer ,ref offset );  
			text.Add( subID.ToString());
			short seqNo =   MessageParser.BufferToInt16( buffer ,ref offset ); 
			text.Add( seqNo.ToString());
	  		m_submessage.ParseMessage( buffer, text,ref offset );

 			return true;
		}
		 */
	}
}
