using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public class CharArrayParameter : Parameter<char[]>
    {

        public override int Length
        {
            get
            {
                return base.Length;
            }
            set
            {
                base.Length = value;
                Value = new char[value];
            }
        }

        public CharArrayParameter()
        {
            Length = 0;
        }

        public override string ToString()
        {
            String s = "";
            foreach (char c in Value)
            {
                s += (c == '\0' ? @"\0" :  c.ToString()) + ',';
            }
            return Name + " [" + (s != "" ? s.Substring(0, s.Length - 1) : s) + "] ";
        }

        public override void FromString(String _str)
        {
            for (int i = 0; i < Length; i++)
            {
                Value[i] = i < _str.Length ? _str[i] : (char)0;
            }
        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {

            for (int i = 0; i < Length; i++)
            {
                buffer[offset + i] = Convert.ToByte(Value[i]);
            }
            Buffer.BlockCopy(Value, 0, buffer, offset, Length);
        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            for( int i = 0 ; i < Length ; i++ )
            {
                Value[i] = Convert.ToChar(buffer[offset + i]);
            }
            
        }

    }
}
