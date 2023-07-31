using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;


namespace AirflowDeviceSim
{
    class MessageFactorySmartflow : IMessageFactory
    {
        public int ReceivingHedaerLenght { get { return 12; } }
        public int SendingHeaderLenght { get { return 12; } }

        public MessageHeader CreateReceivingHeader()
        {
            return new SmartflowHeader(0);
        }

        public MessageHeader AddMessageDefinition(String name, String id)
        {
            if (id == "sf_keep_alive")
            {
                return new SmartflowHeader(1);
            }
            if (id == "sf_route_decision")
            {
                return new SmartflowHeader(6);
            }
            return null;
        }


        public Message CreateMessage(MessageHeader _hdr, Direction _dir)
        {
            Message msg = null;
            if (_hdr is SmartflowHeader)
            {

                switch (((SmartflowHeader)_hdr).MessageType)
                {
                    case 1:
                        _hdr.Name = "Keep Alive";
                        msg = new SmartflowMessage((SmartflowHeader)_hdr);
                        msg.AddField<UInt16Parameter>("ActiveFlag");
                        msg.WrapUp();
                        //if (MessageMap.ContainsKey(((SACMessageHeader)_hdr).MessageType))
                        //{
                        //    Tuple<string, Action<Message, Direction>> creater = MessageMap[((SACMessageHeader)_hdr).MessageType];
                        //    msg = new SACMessage(_hdr as SACMessageHeader);
                        //    msg.Header.Name = creater.Item1;
                        //    creater.Item2(msg, _dir);
                        //    msg.WrapUp();
                        //}
                        break;
                    case 6:
                        _hdr.Name = "Route Decision";
                        msg = new SmartflowMessage((SmartflowHeader)_hdr);
                        if (_dir == Direction.Send)
                        {
                            msg.AddField<UInt32Parameter>("TrayId");
                            msg.AddField<UInt16Parameter>("TrayType");
                            msg.AddField<UInt16Parameter>("Destination");
                            msg.AddField<UInt16Parameter>("TrayStatus");
                            msg.AddField<UInt32Parameter>("GlobalUd");
                            msg.AddField<StringParameter>("Barcode").SetLength(12, 10).Padding = Padding.SPACE;
                            msg.AddField<UInt16Parameter>("ItemStatus");
                            msg.AddField<UInt16Parameter>("RecircCount");
                            msg.AddField<UInt16Parameter>("EDSRecirc");
                            msg.AddField<UInt16Parameter>("Screening");
                            msg.WrapUp();
                        }
                        else
                        {
                            msg.AddField<UInt32Parameter>("TrayId");
                            msg.AddField<UInt16Parameter>("Dest");
                            msg.AddField<UInt16Parameter>("Alt Dest");
                            msg.AddField<UInt16Parameter>("Error");
                            msg.AddField<UInt16Parameter>("RS 1");
                            msg.AddField<UInt16Parameter>("RS 2");
                            msg.AddField<UInt16Parameter>("RS 3");
                            msg.AddField<UInt16Parameter>("RS 4");
                            msg.AddField<UInt16Parameter>("RS 5");
                            msg.AddField<UInt16Parameter>("RS 6");
                            msg.AddField<UInt16Parameter>("RS 7");
                            msg.AddField<UInt16Parameter>("RS 8");
                            msg.AddField<UInt16Parameter>("RS 9");
                            msg.AddField<UInt16Parameter>("RS 10");
                        }
                        break;
                }
            }
            return msg;
        }

    }
}
