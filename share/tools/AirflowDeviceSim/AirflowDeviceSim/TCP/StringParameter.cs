using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public enum Padding { NULL, SPACE, ZERO }


    public class StringParameter : Parameter<String>
    {

        char m_Padding;

        public override string Value
        {
            get
            {
                return base.Value;
            }
            set
            {
                if (value != null && value.Length > Length)
                {
                    base.Value = value.Substring(0, Length);
                }
                else
                {
                    base.Value = value;
                }
            }
        }

        public Padding Padding
        {
            get
            {
                if (m_Padding == '0')
                {
                    return TCP.Padding.ZERO;
                }
                if (m_Padding == ' ')
                {
                    return TCP.Padding.SPACE;
                }
                return TCP.Padding.NULL;
            }
            set
            {
                if (value == TCP.Padding.ZERO)
                {
                    m_Padding = '0';
                }
                else if (value == TCP.Padding.SPACE)
                {
                    m_Padding = ' ';
                }
                else
                {
                    m_Padding = '\0';
                }
            }
        }


        public StringParameter()
        {
            Padding =  TCP.Padding.NULL;
        }


        public StringParameter SetLength(int _length, int _dataEntryLength )
        {
            Length = _length;
            DataEntryLength = _dataEntryLength;
            return this;
        }

        public StringParameter SetLength(int _length)
        {
            return SetLength(_length, _length);
        }

        public StringParameter SetPadding(Padding _padding)
        {
            Padding = _padding;
            return this;
        }

        public override string ToString()
        {
            return Name + " [" + (Value == null ? "" : Value.ToString()) + "] ";
        }

        public override void FromString(String _str)
        {
            Value = _str;
        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
            ASCIIEncoding enc = new ASCIIEncoding();
            byte[] data = enc.GetBytes( (Value== null ? String.Empty : Value ));
            Buffer.BlockCopy(data, 0, buffer, offset, Math.Min(Length, data.Length));
            for (int idx = offset + data.Length; idx < offset + Length; idx++)
            {
                buffer[idx] = Convert.ToByte(m_Padding);
            }
        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            ASCIIEncoding enc = new ASCIIEncoding();
            Value = enc.GetString(buffer, offset, Length).Replace('\0', ' ').Trim();
        }
    }
}
