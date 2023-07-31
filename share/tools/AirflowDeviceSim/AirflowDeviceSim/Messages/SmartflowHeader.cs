using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim
{
    public class SmartflowHeader : MessageHeader
    {

        UInt16Parameter MessageTypeParam;

        public ushort MessageType
        {
            get
            {
                return MessageTypeParam.Value;
            }
        }

        public SmartflowHeader(ushort messageType): base()
        {
            //MessageType = msgID;
            MessageTypeParam = AddField<UInt16Parameter>("Message Type", true);
            MessageTypeParam.Value = messageType;
            AddField<UInt16Parameter>("Equipment Id");
            AddField<UInt16Parameter>("Serial No");
            AddField<ByteParameter>("Hour");
            AddField<ByteParameter>("Minute");
            AddField<UInt16Parameter>("Millisecond");
            AddField<UInt16Parameter>("Length", true);
        }
    }
}
