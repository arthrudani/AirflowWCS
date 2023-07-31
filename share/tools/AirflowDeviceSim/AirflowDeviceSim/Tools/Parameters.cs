using System;
using System.Collections.Generic;
using System.Text;
using System.Windows.Forms;


namespace PostcodeGUI.Tools
{
    //6 words 2  ASCI characters each 
    /*
      6 words - two 8 bit characters per word.  The ID is a contiguous string starting with the upper 8 bits of the first word.
      Each subsequent character is written into the next 8 bits in the message (1st word lower 8 bits, 2nd word upper 8 bits, 
      2nd word lower 8 bits, etc....)  (i.e., left justified).  Any blank characters on the right of the field will remain
      as the LCS sent them.

      UUU######CC  where:

      UUU - is the 3 alphanumeric character IATA code that defines the ULD type
      ###### - is the 4, 5 or 6 numeric sequence for the ULD’s
      CC - is the 2 character carrier ID
    */
    //Parameter lengths in Bytes

    class ParaneterInfo
    {
        public static int UnitIDLength      = 12;
        public static int LocationLength    = 6;
        public static int ToDivertLength    = 6;
        public static int FromLength        = 6;
        public static int FinalLength        = 6;

        public static String UnitIDName     = "Unit ID";
        public static String UnitSizeName   = "Unit Size";
        public static String LosationName   = "Location";
        public static String ToDivertName   = "To Divert";
        public static String FromName       = "From";
        public static String FinalName      = "Final";
        public static String WeightName     = "Weight";

        public static String OperatorIdName                             = "Operator ID";
        public static String ModeStateDirectionName                     = "Mode State Direction";
        public static String VehicleActivitySubMaintAvailabilityName    = "Vehicle Activity ";
        public static String NotInUseName                               = "Not Used";


        public static String NotInUseText = "NA";
        public static String DefaultText = "0000000";

        public static String NotInUseNumber = "0000000";
        public static String DefaultNumber = "0000000";


    }

#region ---- DateTime ------

	public  class DateTimeParam : BaseParameter
	{
		private const int DataEntryLength = 5; //assuming signed 16 bit -32768 to 32767
		private  DateTime  _Data; 
		//private static String DefFormat ="G";// "dd/MM/YY";//:HH:mm:ss";
		private static String DefFormat = "dd/MM/yyyy HH:mm:ss.fff";//:HH:mm:ss";

		private String _Format;

		public DateTime Data
		{
			get
			{
				return _Data;
			}
			set
			{
				_Data = value;
				Invalidate();
			}
		}

		public String Format
		{
			set
			{
				_Format = value;
			}
		}

		public DateTimeParam( String caption )
		{
			m_caption = caption ;
			m_size = 8;
			Data = new DateTime();
			_Format = DefFormat;
		}


		public override void FromString( String stringData )
		{
			//m_data = Convert.ToInt16( stringData );
		}

		public override string ToString( )
		{
			if( Data > DateTime.MinValue )
			{
				return Data.ToString( _Format );
			}
			return( "Time Not Set" );
		}

		public override int GetDataEntryLength( )
		{
			return DataEntryLength;
		}

		public override void WriteToByteArray( Byte[] buffer , ref int offset )
		{
			long ft = Data.ToFileTimeUtc();
			Byte[] rempAttay = BitConverter.GetBytes( ft );
			Buffer.BlockCopy( rempAttay , 0 , buffer , offset , m_size );
			offset += m_size;	
		}

		public override bool OnReadFromByteArray( Byte[] buffer , ref int offset )
		{
			//Convert from file time
			//UInt32 lowFileTime = MessageOnReadFromByteArrayr.BufferToUInt32( buffer , ref offset );
			//UInt32 highFileTime = MessageOnReadFromByteArrayr.BufferToUInt32( buffer,ref offset );
	 		//long hFT2 = ( ( ( long )highFileTime ) << 32 ) + lowFileTime;
			bool bUpdate = false;
			ulong hFT2 = MessageParser.BufferToUInt64( buffer, ref offset );

			if( DataValid == true )
			{
				if( hFT2 != ( ulong )Data.ToFileTimeUtc() )
				{
					bUpdate = true;
				}
			}
			else
			{
				bUpdate = true;
			}

			if( bUpdate == true )
			{
				if( hFT2 > 0 )
				{
					try
					{
						Data = DateTime.FromFileTimeUtc( ( long )hFT2 );
						DataValid = true;
					}
					catch( Exception )
					{
						Data = DateTime.MinValue;
						DataValid = false;
					}
				}
				else
				{
					Data = DateTime.MinValue;
					DataValid = false;
				}
			}
			return bUpdate;
		}
	}

#endregion 
#region	 -------- AsciAndNumber
	/*
	class AsciAndNumber : CharArray
	{
		const int NumSataLen		= 2;
		const int DataEntryLength	= 8;

		short m_numData;


	    public AsciAndNumber( string caption ) : base(caption ,  4 )
		{
			m_numData = 0;
			m_caption = String.Format("{0} ({1}) ", caption, DataEntryLength);
		}

		public override void FromString(String stringData)
		{
			//4 bytes of ASCI chars
			base.FromString( stringData );
			//get numeric part if lenght is greter than 4
			if (stringData.Length > m_size)
			{
				String num = stringData.Remove(0,m_size);
				m_numData = Convert.ToInt16(num);
			}
			else
			{
				m_numData = 0;
			}
 		}

		public override string ToString()
		{
			return String.Format("{0}{1}",base.ToString(), m_numData.ToString());
		}

		public override void WriteToByteArray(Byte[] buffer, ref int offset)
		{
			base.WriteToByteArray( buffer, ref offset );

			Byte[] rempAttay = BitConverter.GetBytes(m_numData);
			Buffer.BlockCopy(rempAttay, 0, buffer, offset, NumSataLen);
			offset += NumSataLen;
		}

		public override int GetDataEntryLength()
		{
			return DataEntryLength;
		}
   
		public override int OnReadFromByteArray( Byte[] buffer,ref int offset )
		{
			base.OnReadFromByteArray( buffer, ref offset );
			m_numData =  MessageParser.BufferToInt16( buffer, ref offset );
			return 1;
		}
	}
	 */ 
	#endregion
	
	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Fixed size passed in constructor
    public class CharArray : BaseParameter
    {
        protected byte[] m_data;

        public CharArray(String caption, int size ) 
        {
            m_data      = new byte[size];
            m_caption   = String.Format("{0} ({1})", caption , size ) ;
            m_size      = size;
             
            FromString(ParaneterInfo.DefaultText);
        }

        
        public override void FromString(String stringData)
        {
            //3 alpha neumerics
            int index = 0;
            for (; index < stringData.Length && index < m_size ; index++)
            {
                m_data[index] = Convert.ToByte(stringData[index]);
            }
            for (; index < m_size; index++)
            {
                m_data[index] = 0;
            }
        }

        public override string ToString()
        {
            char[] str = new char[m_size];
            for (int i = 0; i < m_size; i++)
            {
                str[i] = (char)m_data[i];
            }
            return new String(str);
        }

        public override void WriteToByteArray(Byte[] buffer, ref int offset)
        {
            Buffer.BlockCopy(m_data, 0, buffer, offset, m_size );
            offset += m_size;
        }

		public override bool OnReadFromByteArray( Byte[] buffer , ref int  offset )
		{

			Buffer.BlockCopy( buffer , offset , m_data , 0 , m_size );
			offset+=m_size;
			return true;
		}

    }

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public class ByteBuffer :  CharArray
	{

		public ByteBuffer( String caption , int size ) : base( caption, size )
		{
			
		}
				
		public override void WriteToByteArray( Byte[] buffer , ref int offset )
		{
			Buffer.BlockCopy( m_data , 0 , buffer , offset , m_size );
			offset += m_size;
		}

		public override bool OnReadFromByteArray( Byte[] buffer , ref int offset )
		{
			m_size=	   buffer.Length - offset ;
			Array.Resize( ref m_data, m_size );

			Buffer.BlockCopy( buffer , offset , m_data , 0 , m_size );
			offset += m_size;
			return true;
		}

	}

	//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	//Parameter to handle STRING type in messages
	public class ByteString : CharArray
	{

		public override int Size
		{
			get
			{
				return m_size + 2; // To support 2 bytes for length parameter
			}
		}


		public ByteString( String caption , String data , int size )
			: this(	caption,size )
		{
			FromString( data );
		}


		public ByteString( String caption , int size )
			: base( caption , size )
		{
		}

		public override void FromString( string stringData )
		{
			m_size = stringData.Length + 1;
			Array.Resize( ref m_data , m_size );
			base.FromString( stringData );
		}


		public override void WriteToByteArray( Byte[] buffer , ref int offset )
		{
			UInt16 len = (UInt16)m_size ;
			Byte[] rempAttay = BitConverter.GetBytes( len );
			Buffer.BlockCopy( rempAttay , 0 , buffer , offset , 2 );
			offset+=2;
			Buffer.BlockCopy( m_data , 0 , buffer , offset , m_size );
			offset += m_size ;
		}

		public override bool OnReadFromByteArray( Byte[] buffer , ref int offset )
		{
			//Find the null terminater
			byte term = 0 ;
			UInt16 length = MessageParser.BufferToUInt16( buffer , ref offset );
			//Increment to ignore length byte
			
			int idx = Array.IndexOf(buffer , term , offset );
			if( idx == -1 ) //noterminator
			{
				idx =   offset + length;
			}
		 	m_size =  idx - offset + 1 ;
			Array.Resize( ref m_data , m_size );

			Buffer.BlockCopy( buffer , offset , m_data , 0 , m_size );
			offset += m_size;
			return true;
		}

		public override string ToString( )
		{
			char[] str = new char[ m_size ];
			for( int i = 0 ; i < m_size ; i++ )
			{
				str[ i ] = ( char )m_data[ i ];
			}
			return new String( str ,0,m_size - 1 );
		}
	}

	/* 
	 * DWord Param 
	 * Used for unit ID size
	*/
	public  class DWordParam : BaseParameter
	{
		private const int DataEntryLength = 5; //assuming signed 16 bit -32768 to 32767
		private UInt32 m_data;

		public UInt32 Data
		{
			get
			{
				return m_data;
			}
			set
			{
				if( m_data != value || _DataValid == false )
				{
					m_data = value;
					_DataValid = true;
					Invalidate();
				}
			}
		}

		public DWordParam( String caption )
		{
			m_caption = caption + " ( int )";
			m_size = 4;
			m_data = 0;
		}


		public override void FromString( String stringData )
		{
			m_data = Convert.ToUInt32( stringData );
		}

		public override string ToString( )
		{
			if( _DataValid )
			{
				return m_data.ToString();
			}
			return "Unknown";
		}

		public override int GetDataEntryLength( )
		{
			return DataEntryLength;
		}

		public override void WriteToByteArray( Byte[] buffer , ref int offset )
		{
			Byte[] rempAttay = BitConverter.GetBytes( m_data );
			Buffer.BlockCopy( rempAttay , 0 , buffer , offset , m_size );
			offset += m_size;
		}

		public override bool OnReadFromByteArray( Byte[] buffer , ref int offset )
		{
			UInt32 newData = MessageParser.BufferToUInt32( buffer , ref offset );
			if( m_data != newData )
			{
				m_data = newData;
				return true;
			}
			return false;
		}
	}

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++



/* 
 * Word Param 
 * Used for unit ID size
*/
    class WordParam : BaseParameter
    {
        private const int DataEntryLength = 5; //assuming signed 16 bit -32768 to 32767
        public  ushort Data;




        public WordParam(String caption )
        {
            m_caption = caption + " ( int )";
            m_size = 2;
            Data = 0;
        }

        
        public override void FromString(String stringData)
        {
             Data = Convert.ToUInt16(stringData);
        }
       
        public override string ToString()
        {
            return Data.ToString();
        }

        public override int GetDataEntryLength()
        {
            return DataEntryLength;
        }

        public override void WriteToByteArray(Byte[] buffer, ref int offset)
        {
            Byte[] rempAttay = BitConverter.GetBytes(Data);
            Buffer.BlockCopy(rempAttay, 0, buffer, offset, m_size);
            offset += m_size;
        }

		public override bool OnReadFromByteArray( Byte[] buffer ,  ref int offset )
		{
			UInt16 newData = MessageParser.BufferToUInt16( buffer, ref offset );
			if( Data != newData )
			{
				Data = newData;
				return true;
			}
			return false;
		}
    }



	public class ByteParam : BaseParameter
	{
		private const int DataEntryLength = 1; //assuming signed 16 bit -32768 to 32767
		public Byte Data;

		public ByteParam( byte value , String caption )
		{
	 		m_caption = caption;
			m_size = 1;
			Data = value;
	 	}


		public ByteParam( String caption ) : this( 0 , caption )
		{
			
		}


		public override void FromString( String stringData )
		{
			Data = Convert.ToByte( stringData );
		}

		public override string ToString( )
		{
			return Data.ToString();
		}

		public override int GetDataEntryLength( )
		{
			return DataEntryLength;
		}

		public override void WriteToByteArray( Byte[] buffer , ref int offset )
		{
			buffer[offset ] = Data;
			offset += m_size;
		}

		public override bool OnReadFromByteArray( Byte[] buffer , ref int offset )
		{
			bool bUpdate = false;
			byte lastData = Data;
			Data = MessageParser.BufferToByte( buffer , ref offset );
			if( Data != lastData )
			{
				bUpdate = true;	
			}
			if( DataValid == false )
			{
				DataValid = true;
			}
			return bUpdate;
		}
	}

 }
