using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;


namespace AirflowDeviceSim
{
    class MessageFactoryPLC : IMessageFactory
    {
        public int ReceivingHedaerLenght { get { return 16; } }
        public int SendingHeaderLenght { get { return 16; } }

        public MessageHeader CreateReceivingHeader()
        {
            return new PLCMessageHeader(0);
        }

        public MessageHeader AddMessageDefinition(String name, String id)
        {
            if (id == "plc_keep_alive")
            {
                return new PLCMessageHeader(1);
            }else if (id == "plc_move_order")//This Move Order
            {
                return new PLCMessageHeader(2);//type=2
            }else if (id == "plc_move_ack")
            {
                return new PLCMessageHeader(102);
            }
            else if (id == "plc_item_stored")
            {
                return new PLCMessageHeader(52);
            }
            else if (id == "plc_stored_ack")
            {
                return new PLCMessageHeader(152);
            }
            else if (id == "plc_item_rarrived")
            {
                return new PLCMessageHeader(51);
            }
            else if (id == "plc_item_arrived_ack")
            {
                return new PLCMessageHeader(151);
            }
            else if (id == "plc_item_pickedup")
            {
                return new PLCMessageHeader(53);
            }
            else if (id == "plc_item_pickedup_ack")
            {
                return new PLCMessageHeader(153);
            }
            else if (id == "plc_location_status")
            {
                return new PLCMessageHeader(60);
            }
            else if (id == "plc_location_status_ack")
            {
                return new PLCMessageHeader(160);
            }
            
            else if (id == "plc_flight_data_update")
            {
                return new PLCMessageHeader(9);
            }
            else if (id == "plc_flight_data_update_ack")
            {
                return new PLCMessageHeader(109);
            }
            else if (id == "plc_bag_data_update")
            {
                return new PLCMessageHeader(10);
            }
            else if (id == "plc_bag_data_update_ack")
            {
                return new PLCMessageHeader(110);
            }
            else if (id == "plc_Link_StartUp_Sync")
            {
                return new PLCMessageHeader(99);
            }
            return null;
        }


        public Message CreateMessage(MessageHeader _hdr, Direction _dir)
        {
            Message msg = null;
            if (_hdr is PLCMessageHeader)
            {

                switch (((PLCMessageHeader)_hdr).MessageType)
                {
                    case 1:
                        _hdr.Name = "Keep Alive"; //in and out
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("ActiveFlag");
                        msg.WrapUp();
                        break;
                    case 2:
                        _hdr.Name = "Move Order"; //Direction always out
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemId").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<StringParameter>("FlightNumber").SetLength(8); //(example: QFA1234A)
                        msg.AddField<StringParameter>("FlightScheduledDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt32Parameter>("FinalSortLoationId");
                        msg.AddField<UInt32Parameter>("FromLoationId");
                        msg.AddField<UInt32Parameter>("ToLoationId");
                        msg.AddField<UInt16Parameter>("MoveType");
                        msg.WrapUp();
                        break;
                    case 102:
                        _hdr.Name = "Move Order Ack"; //Direction always in
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 3:
                        _hdr.Name = "Flush Request"; //Direction always IN
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("LaneId");
                        msg.AddField<UInt16Parameter>("Quantity");
                        msg.AddField<UInt16Parameter>("ReleaseInterval");
                        msg.WrapUp();
                        break;
                    case 103:
                        _hdr.Name = "Flush Response Ack"; //Direction always Out                     
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 51:
                        _hdr.Name = "Item Arrived"; //Direction always Out ACP -> Airflow WCS
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemId").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("StationId");
                        msg.WrapUp();
                        break;
                    case 151:
                        _hdr.Name = "Item Arrived Ack"; //Airflow WCS → ACP
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 52:
                        _hdr.Name = "Item Stored"; //Direction always Out ACP -> Airflow WCS
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemId").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("LocationId");
                        msg.AddField<UInt16Parameter>("Status"); //1 = succeed, 2 = Error, 3 = Bin Full Error
                        msg.WrapUp();
                        break;
                    case 152:
                        _hdr.Name = "Item Stored Ack"; //Airflow WCS → ACP
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 53:
                        _hdr.Name = "Item Picked Up"; 
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemId").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("FromLocationId");
                        msg.AddField<UInt32Parameter>("ToLocationId"); 
                         msg.AddField<UInt16Parameter>("Status"); //1 = succeed, 2 = Error, 3 = Unexpected Bin Empty Error
                        msg.WrapUp();
                        break;
                    case 153:
                        _hdr.Name = "Item Picked Up Ack"; 
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 60: 
                        _hdr.Name = "Location Status"; 
                       
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("LocationId1");
                        msg.AddField<UInt16Parameter>("Status1");
                        msg.AddField<UInt32Parameter>("LocationId2");
                        msg.AddField<UInt16Parameter>("Status2");

                        msg.WrapUp();
                        break;
                    case 160:
                        _hdr.Name = "Location Status Ack"; //Airflow WCS → ACP
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 10:
                        _hdr.Name = "Flight Data Update"; //
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<StringParameter>("FlightNumber").SetLength(8); //(example: QFA 1234A)
                       // msg.AddField<StringParameter>("FlightScheduledDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt32Parameter>("FinalSortLoationId");
                        msg.WrapUp();
                        break;
                    case 110:
                        _hdr.Name = "Flight Data Update Ack"; //
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 9:
                        _hdr.Name = "Bag Data Update"; //
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemId").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<StringParameter>("FlightNumber").SetLength(8); //(example: QFA 1234A)
                        // msg.AddField<StringParameter>("FlightScheduledDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt32Parameter>("FinalSortLoationId");
                        msg.AddField<UInt32Parameter>("LoationId"); //	Storage location involved in the update
                        msg.AddField<UInt16Parameter>("UpdateType"); //1=Placed in storage location, 2=Removed from storage location, 3=Sort destination update
                        msg.WrapUp();
                        break;
                    case 109:
                        _hdr.Name = "Bag Data Update Ack"; //
                        msg = new PLCMessage((PLCMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 99:
                        _hdr.Name = "Link Startup Sync";
                        msg = new PLCMessage((PLCMessageHeader)_hdr);

                        break;

                }
            }
            return msg;
        }


        byte[] buildLocationStatus()
        {
            /*
            int startingPosition = 0;
            short[] firstArray = new short[] { 55, 1 };
            short[] secondArray = new short[] { 44, 2 };

            byte[] result = new byte[(firstArray.Length + secondArray.Length) * sizeof(short)];
            Buffer.BlockCopy(firstArray, 0, result, startingPosition, 4);

            startingPosition += 4;

            Buffer.BlockCopy(secondArray, 0, result, startingPosition, 4);
            return result;
             * */

            int startingPosition = 0;
            short a = 5;
            short b = 6;
            short a1 = 1;
            short b1 = 2;
            byte[] aByte = BitConverter.GetBytes(a);
            byte[] bByte = BitConverter.GetBytes(b);
            byte[] a1Byte = BitConverter.GetBytes(a1);
            byte[] b1Byte = BitConverter.GetBytes(b1);

            byte[] result = new byte[aByte.Length + bByte.Length + a1Byte.Length+ b1Byte.Length ];
            Buffer.BlockCopy(aByte, 0, result, startingPosition, aByte.Length);
            startingPosition += aByte.Length;
            Buffer.BlockCopy(a1Byte, 0, result, startingPosition, a1Byte.Length);
            startingPosition += a1Byte.Length;
            Buffer.BlockCopy(bByte, 0, result, startingPosition, bByte.Length);
            startingPosition += bByte.Length;
            Buffer.BlockCopy(b1Byte, 0, result, startingPosition, b1Byte.Length);
            return result;
        }

       
    }
}
