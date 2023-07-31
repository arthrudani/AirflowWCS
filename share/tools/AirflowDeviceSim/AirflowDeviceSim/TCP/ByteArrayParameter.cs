using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public class ByteArrayParameter : Parameter<Byte[]>
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
                Value = new byte[value];
            }
        }

        public ByteArrayParameter() 
        {
            Length = 0;
        }


        public override string ToString()
        {
            String s = "";
            foreach (byte b in Value)
            {
                s += b.ToString() + ',';
            }
            return Name + " [" + (s != "" ? s.Substring(0, s.Length - 1) : s) + "] ";
        }

        public override void FromString(String _str)
        {

        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
             Buffer.BlockCopy(Value, 0, buffer, offset, Length);
         }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
        
            //Length = 8;
            //Buffer.BlockCopy(buffer, offset, Value, 0, 8);
             Buffer.BlockCopy(buffer, offset, Value, 0, Length);
        }


    }
}
