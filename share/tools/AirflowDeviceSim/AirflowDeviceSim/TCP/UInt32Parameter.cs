using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

namespace AirflowDeviceSim.TCP
{
    public class UInt32Parameter : Parameter<UInt32>
    {
        public UInt32Parameter()
        {
            Length = 4;
            DataEntryLength = 10;
        }

        public override void FromString(String _str)
        {
            Value = UInt32.Parse(_str);
        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
            Buffer.BlockCopy(BitConverter.GetBytes( netByteOrder ? (UInt32)IPAddress.HostToNetworkOrder( (Int32)Value ) :  Value), 0, buffer, offset, Length);
          
        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            Value = BitConverter.ToUInt32(buffer, offset);
            if (netByteOrder)
            {
                Value = (UInt32) IPAddress.NetworkToHostOrder((Int32)Value);
            }
        }
    }
}
