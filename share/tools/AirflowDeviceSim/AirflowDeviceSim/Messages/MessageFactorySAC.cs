using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim.Messages
{
    class MessageFactorySAC : IMessageFactory
    {
        public int ReceivingHedaerLenght { get { return 16; } }
        public int SendingHeaderLenght { get { return 16; } }

        public MessageHeader CreateReceivingHeader()
        {
            return new SACMessageHeader(0);
        }

        public MessageHeader AddMessageDefinition(string name, string id)
        {
            if (id == "sac_keep_alive")
            {
                return new SACMessageHeader(1);
            }
            else if (id == "sac_expected_rceipt")
            {
                return new SACMessageHeader(52);
            }
            else if (id == "sac_expected_rceipt_ack")
            {
                return new SACMessageHeader(152);
            }
            else if (id == "sac_expected_rceipt_response")
            {
                return new SACMessageHeader(2);
            }
            else if (id == "sac_expected_rceipt_response_ack")
            {
                return new SACMessageHeader(102);
            }
            else if (id == "sac_stored_complete")
            {
                return new SACMessageHeader(3);
            }
            else if (id == "sac_stored_complete_ack")
            {
                return new SACMessageHeader(103);
            }
            else if (id == "sac_retrieve_item") //Retrieve Item 
            {
                return new SACMessageHeader(54);
            }
            else if (id == "sac_retrieve_item_ack")
            {
                return new SACMessageHeader(154);
            }
            else if (id == "sac_retrieve_item_response") 
            {
                return new SACMessageHeader(4);
            }
            else if (id == "sac_retrieve_item_response_ack")
            {
                return new SACMessageHeader(104);
            }else if (id == "sac_retrieve_flight")
            {
                return new SACMessageHeader(55);
            }
            else if (id == "sac_retrieve_flight_ack")
            {
                return new SACMessageHeader(155);
            } else if (id == "sac_retrieve_flight_response")
            {
                return new SACMessageHeader(5);
            }
            else if (id == "sac_retrieve_flight_response_ack")
            {
                return new SACMessageHeader(105);
            }
            else if (id == "sac_item_released")
            {
                return new SACMessageHeader(6);
            }
            else if (id == "sac_item_released_ack")
            {
                return new SACMessageHeader(106);
            }
            else if (id == "sac_flight_data_update")
            {
                return new SACMessageHeader(57);
            }
            else if (id == "sac_flight_data_update_ack")
            {
                return new SACMessageHeader(157);
            }
            else if (id == "sac_inventory_update")
            {
                return new SACMessageHeader(7);
            }
            else if (id == "sac_inventory_update_ack")
            {
                return new SACMessageHeader(107);
            }


            return null;
        }

        public Message CreateMessage(MessageHeader _hdr, Direction _dir)
        {
            Message msg = null;
            if (_hdr is SACMessageHeader)
            {
                switch (((SACMessageHeader)_hdr).MessageType)
                {
                    case 1:
                        _hdr.Name = "Keep Alive SAC"; //in and out
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("ActiveFlag");
                        msg.WrapUp();
                        break;
                    case 52:
                        _hdr.Name = "Expected Receipt"; 
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                         msg.AddField<UInt32Parameter>("TrayId"); //Container ID (Tray)
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemID").SetLength(12);
                        msg.AddField<StringParameter>("FlightNumber").SetLength(8); //(example: QFA 1234A)
                        msg.AddField<StringParameter>("FlightScheduledDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<StringParameter>("DefaultRetrievalDateTime").SetLength(14); //Default Retrieval Date Time -(YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt32Parameter>("FinalSortLocationID");
                        msg.AddField<UInt16Parameter>("ItemType");   //Item Type (Standard = 1, Oversize = 2)
                        msg.AddField<UInt16Parameter>("RequestType");//Request Type (New = 1, Update = 2, Cancel =3)
                        msg.WrapUp();
                        break;
                    case 152:
                        _hdr.Name = "Expected Receipt Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 2:
                        _hdr.Name = "Expected Receipt Response";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId"); //Container ID (Tray)
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("ItemID").SetLength(12);
                        msg.AddField<UInt32Parameter>("EntranceStationID");
                        msg.AddField<UInt16Parameter>("Status"); 
                        msg.WrapUp();
                        break;
                    case 102:
                        _hdr.Name = "Expected Receipt Response Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 3:
                        _hdr.Name = "Storage Complete"; 
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("OrderId");
                        msg.AddField<UInt32Parameter>("TrayId"); //Container ID (Tray)
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("BagID").SetLength(12);
                        msg.AddField<UInt16Parameter>("ZoneId");
                        msg.AddField<UInt32Parameter>("LocationId");
                        msg.AddField<UInt16Parameter>("Status");  //Status (2=Successed, 4=Failed)
                        msg.WrapUp();
                        break;
                    case 103:
                        _hdr.Name = "Storage Complete Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 54:
                        _hdr.Name = "Retrieve Item"; //Direction always IN
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("OrderID");
                        msg.AddField<UInt16Parameter>("ArrayLength");
                        // in this test we put three items/trays/bags in the array
                        //first item:
                        msg.AddField<UInt32Parameter>("TrayId_1");
                        msg.AddField<UInt32Parameter>("GlobalId_1");
                        msg.AddField<StringParameter>("BagID_1").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("StorageLocationID_1");

                        //second item:
                        msg.AddField<UInt32Parameter>("TrayId_2");
                        msg.AddField<UInt32Parameter>("GlobalId_2");
                        msg.AddField<StringParameter>("BagID_2").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("StorageLocationID_2");

                        //third item:
                        msg.AddField<UInt32Parameter>("TrayId_3");
                        msg.AddField<UInt32Parameter>("GlobalId_3");
                        msg.AddField<StringParameter>("BagID_3").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("StorageLocationID_3");
                        msg.WrapUp();
                        break;
                    case 154:
                        _hdr.Name = "Retrieve Item Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 4:
                        _hdr.Name = "Retrieve Item Response"; 
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("OrderID");
                        msg.AddField<UInt16Parameter>("Status");  
                        msg.AddField<UInt16Parameter>("ArrayLenght"); 
                        //TODO: missing items array 
                       
                        msg.WrapUp();
                        break;
                    case 104:
                        _hdr.Name = "Retrieve Item Response Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 55:
                        _hdr.Name = "Retrieve Flight"; //Direction always IN
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("OrderID");
                        msg.AddField<StringParameter>("FlightNum").SetLength(8); //Flight Number (example: QFA1234A)
                        msg.AddField<StringParameter>("FlightScheduledDT").SetLength(14); //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt16Parameter>("NumOfBagsToRetrieve"); //Number of Bags to retrieve ( 0=All )
                        msg.WrapUp();
                        break;
                    case 155:
                        _hdr.Name = "Retrieve Flight Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;  
                    case 5:
                        _hdr.Name = "Retrieve Flight Response";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("OrderID");
                        msg.AddField<UInt16Parameter>("Status");
                        msg.AddField<UInt16Parameter>("NumOfMissingBags");
                        //TODO: missing items array 

                        msg.WrapUp();
                        break;
                    case 105:
                        _hdr.Name = "Retrieve Flight Response Ack";
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break; 
                    
                    case 6:
                        _hdr.Name = "Item Released"; //Direction always Out
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("BagID").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt32Parameter>("OutboundStationId");
                        msg.WrapUp();
                        break;
                    case 106:
                        _hdr.Name = "Item Released Ack"; //Direction always Out
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 57:
                        _hdr.Name = "Flight Data Update"; //
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<StringParameter>("FlightNumber").SetLength(8); //(example: QFA 1234A)
                        msg.AddField<StringParameter>("FlightScheduledDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<StringParameter>("DefaultRetrievalDateTime").SetLength(14);  //Flight Scheduled Date Time - STD (YYYYMMDDHHMMSS-20221201134500)
                        msg.AddField<UInt32Parameter>("FinalSortLoationId");
                        msg.WrapUp();
                        break;
                    case 157:
                        _hdr.Name = "Flight Data Update Ack"; //
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                    case 7:
                        _hdr.Name = "Inventory Update"; //Direction always Out
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt32Parameter>("TrayId");
                        msg.AddField<UInt32Parameter>("GlobalId");
                        msg.AddField<StringParameter>("BagID").SetLength(12, 10).Padding = Padding.SPACE;
                        msg.AddField<UInt16Parameter>("Status"); //0=Automatically removed from storage location, 1=Manually removed from a storage location, 2=manual update added to a location, 3=pickup failed – nothing to pick up.
                        msg.WrapUp();
                        break;
                    case 107:
                        _hdr.Name = "Inventory Update Ack"; //
                        msg = new SACMessage((SACMessageHeader)_hdr);
                        msg.AddField<UInt16Parameter>("Status"); //Status (0=succeed, 2=Error, 3=Error in Serial Num)
                        msg.WrapUp();
                        break;
                }
            }
            return msg;
        }
    }
}
