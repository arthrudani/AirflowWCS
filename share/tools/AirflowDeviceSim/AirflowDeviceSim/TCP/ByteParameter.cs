using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public class ByteParameter : Parameter<Byte>
    {
        public ByteParameter()
        {
            Length = 1;
            DataEntryLength = 3;
        }

        public override void FromString(String _str)
        {
            Value = Byte.Parse(_str);
        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
            buffer[offset] = Value;
        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            Value = buffer[offset];
        }
    }
}
