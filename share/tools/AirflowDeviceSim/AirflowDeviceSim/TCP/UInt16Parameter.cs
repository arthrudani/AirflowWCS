using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

namespace AirflowDeviceSim.TCP
{
    public class UInt16Parameter : Parameter<UInt16>
    {
        public UInt16Parameter()
        {
            Length = 2;
            DataEntryLength = 5;
        }

        public override void FromString(String _str)
        {
            Value = UInt16.Parse(_str);
        }

        public override void Write(Byte[] buffer, int offset, bool netByteOrder)
        {
            Buffer.BlockCopy(BitConverter.GetBytes(netByteOrder ? (UInt16)IPAddress.HostToNetworkOrder((Int16)Value) : Value), 0, buffer, offset, 2);
            offset += 2;
        }

        public override void Read(Byte[] buffer, int offset, bool netByteOrder)
        {
            Value = BitConverter.ToUInt16(buffer, offset);
            if (netByteOrder)
            {
                Value = (UInt16)IPAddress.NetworkToHostOrder((Int16)Value);
            }
        }
    }
}
