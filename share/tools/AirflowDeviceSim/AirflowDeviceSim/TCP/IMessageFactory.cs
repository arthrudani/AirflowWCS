using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public enum Direction{ Send , Receive };

    public interface IMessageFactory
    {
        int ReceivingHedaerLenght { get; }
        int SendingHeaderLenght { get; }

        MessageHeader AddMessageDefinition(String name, String id);

        MessageHeader CreateReceivingHeader();
        Message CreateMessage(MessageHeader _hdr , Direction _dir);
    }
}
