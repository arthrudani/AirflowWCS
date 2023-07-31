using System;
using System.Collections.Generic;
using System.Text;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim
{
	public class SACMessageHeader : MessageHeader
	{
        UInt16Parameter MessageTypeParam;

        public ushort MessageType
        {
            get
            {
                return MessageTypeParam.Value;
            }
        }

        public SACMessageHeader(ushort messageType)
			: base( )
		{

            AddField<UInt16Parameter>("Length", true);
            AddField<UInt16Parameter>("Serial No");
            MessageTypeParam = AddField<UInt16Parameter>("Message Type", true);
            MessageTypeParam.Value = messageType;
            AddField<UInt32Parameter>("Equipment Id");
            AddField<ByteParameter>("Hour");
            AddField<ByteParameter>("Minute");
            AddField<UInt16Parameter>("Millisecond");
            AddField<UInt16Parameter>("Version");

		}
	}
}
