using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public class CharParameter : Parameter<char>
    {
        public CharParameter() { Length = 1; }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
            buffer[offset] = (byte)Value;
            //ASCIIEncoding enc = new ASCIIEncoding();
            //byte[] data = enc.GetBytes((Value == null ? String.Empty : Value));
            //Buffer.BlockCopy(data, 0, buffer, offset, Math.Min(Length, data.Length));
            //for (int idx = offset + data.Length; idx < offset + Length; idx++)
            //{
            //    buffer[idx] = 0;
            //}
        }

        public override void FromString(String _str)
        {
            if (_str.Length > 0)
            {
                Value = _str[0];
            }

        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            //ASCIIEncoding enc = new ASCIIEncoding();
            //Value = enc.GetString(buffer, offset, Length);
            Value =(char)buffer[offset];
        }

        public override string ToString()
        {
            return Name + " [" + (char.IsControl(Value) ? "" : Value.ToString()) + "] ";
        }
    }
}
