using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim
{
    public class MessageFactory : IMessageFactory
    {

        public int ReceivingHedaerLenght { get{ return 8;} }
        public int SendingHeaderLenght { get { return 8; } }

        Dictionary<ushort, Tuple<String, Action<Message, Direction>>> MessageMap = new Dictionary<ushort, Tuple<String, Action<Message, Direction>>>();


        public MessageHeader CreateReceivingHeader()
        {
            return new SACMessageHeader(0);
        }

        /// <summary>
        /// Supported messages. This is called according to messages configured in configuration file
        /// </summary>
        /// <param name="name"></param>
        /// <param name="id"></param>
        /// <returns></returns>
        public MessageHeader AddMessageDefinition(String name, String id)
        {
            byte messageType = 0;
            switch( id )
            {
                case "af_keep_alive":
                    messageType = 80;
                    MessageMap[messageType] = new Tuple<string, Action<Message, Direction>>(name, CreateAirflowKeppAlive);
                    break;
                case "af_weight_reconcile":
                    messageType = 53;
                    MessageMap[messageType] = new Tuple<string, Action<Message, Direction>>(name, CreateAirflowWeightReconcile);
                    break;
                case "af_fallback_request":
                    messageType = 52;
                    MessageMap[messageType] = new Tuple<string, Action<Message, Direction>>(name, CreateAirflowFallbackRequest);
                    break;
                case "af_flight_no_request":
                    messageType = 54;
                    MessageMap[messageType] = new Tuple<string, Action<Message, Direction>>(name, CreateAirflowFlightNumberkRequest);
                    break;

            }
            if(messageType > 0 )
            {
                return new  SACMessageHeader(messageType);
            }
            return null;
        }

        /// <summary>
        /// Airflow (rBags) Keep alive
        /// </summary>
        /// <param name="_msg"></param>
        /// <param name="_dir"></param>
        void CreateAirflowKeppAlive(Message _msg, Direction _dir)
        {
            if (_dir == Direction.Receive)
            {
                _msg.AddField<UInt16Parameter>("Status");
                _msg.AddField<UInt16Parameter>("Reserved");
            }
        }

        /// <summary>
        /// Airflow (rBags) weight reconciliation
        ///     //case 53 : //Weight reconciliation request (Air Nz )
        //      //DEVMSGHEADER	Header;
        //      //PDB_BARCODE		Barcode;		//IATA bag tag	
        //      //WORD			wBagWeight;		//Bag weight (Grams)
        //      //DWORD			dwReserved;		//Should be initialised to 0's by PLC
        /// </summary>
        /// <param name="_msg"></param>
        /// <param name="_dir"></param>
        void CreateAirflowWeightReconcile(Message _msg, Direction _dir)
        {
            if (_dir == Direction.Send)
            {
                _msg.AddField<StringParameter>("Barcode").SetLength(12, 10).Padding = Padding.SPACE;
                _msg.AddField<UInt16Parameter>("Weight");
                _msg.AddField<UInt32Parameter>("Reserved");
            }
            else
            {
                _msg.AddField<UInt16Parameter>("Result");
                _msg.AddField<UInt16Parameter>("Process Flags");
            }
        }


        void CreateAirflowFallbackRequest(Message _msg, Direction _dir)
        {
            //case 52 : //Fallback request (to reconcile dummy tag to a real barcode

            if (_dir == Direction.Send)
            {
                _msg.AddField<StringParameter>("Barcode").SetLength(12, 10).Padding = Padding.SPACE;
                _msg.AddField<StringParameter>("Dummy Tag").SetLength(12,10).Padding = Padding.SPACE;
                _msg.AddField<UInt32Parameter>("Reserved");
            }
            else
            {
                _msg.AddField<UInt16Parameter>("Result");
                _msg.AddField<UInt16Parameter>("Process Flags");
                _msg.AddField<UInt16Parameter>("Reserved");
            }
        }

        /// <summary>
        /// Message id 54 flight number request
        /// </summary>
        /// <param name="_msg"></param>
        /// <param name="_dir"></param>
        void CreateAirflowFlightNumberkRequest(Message _msg, Direction _dir)
        {
  
            if (_dir == Direction.Send) //request
            {
                _msg.AddField<StringParameter>("Barcode").SetLength(12, 10).Padding = Padding.SPACE;
                _msg.AddField<UInt32Parameter>("Reserved"); //PLC reference
            }
            else //reply
            {
                _msg.AddField<UInt16Parameter>("Result"); // 1 for success
                _msg.AddField<UInt16Parameter>("Process Flags");
                _msg.AddField<UInt16Parameter>("Flight No");
                _msg.AddField<UInt16Parameter>("Reserved");
            }
        }


        public Message CreateMessage(MessageHeader _hdr, Direction _dir)
        {
            Message msg = null;
            if (_hdr is SACMessageHeader)
            {
                if (MessageMap.ContainsKey(((SACMessageHeader)_hdr).MessageType))
                {
                    Tuple<string, Action<Message, Direction>> creater = MessageMap[((SACMessageHeader)_hdr).MessageType];
                    msg = new SACMessage(_hdr as SACMessageHeader);
                    msg.Header.Name = creater.Item1;
                    creater.Item2(msg, _dir);
                    msg.WrapUp();
                }
            }



            //switch (_hdr.GetField<ByteParameter>("MessageType").Value)
            //{
            //    case 0x50:
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //        msg.Header.Name = "Keep Alive";
            //        if (_dir == Direction.Receive)
            //        {
            //            msg.AddField<UInt16Parameter>("Status");
            //            msg.AddField<UInt16Parameter>("Reserved");
            //        }
            //        break;
            //    case 62 :  // Screening data
            //         msg = new SACMessage(_hdr as SACMessageHeader);
            //         msg.Header.Name = "Screening Data";
            //         if (_dir == Direction.Send)
            //         {
            //             msg.AddField<StringParameter>("Barcode").Length = 12;
            //             msg.AddField<CharParameter>("cLastScreeningLevel");
            //             msg.AddField<CharParameter>("cLastScreeningResult");
            //             msg.AddField<ByteParameter>("Reserved");
            //             msg.AddField<ByteParameter>("X-Ray Id");
            //             msg.AddField<UInt32Parameter>("OperatorId");
            //         }
            //        break;
            //    case 75 : //Trace seen
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //        msg.Header.Name = "Trace Seen";
            //        if (_dir == Direction.Send)
            //        {
            //            msg.AddField<StringParameter>("Barcode").Length = 12;
            //            msg.GetField<StringParameter>("Barcode").Value = "1234567890";
            //            msg.AddField<ByteParameter>("Action");
            //            msg.AddField<ByteParameter>("Reserved");
            //            msg.AddField<UInt32Parameter>("OperatorId");
            //        }
            //        else
            //        {
            //            msg.AddField<UInt32Parameter>("StatusCode");

            //        }
            //        break;
            //    case 106 : //Item decision type 2
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //        msg.Header.Name = "Item Decision Type 2";
            //        if (_dir == Direction.Send)
            //        {
            //            msg.AddField<StringParameter>("Barcode").SetLength(12).Value = "0086000100";
            //            msg.AddField<StringParameter>("ScannetTag1").SetLength(12).Value = "0086000100";
            //            msg.AddField<StringParameter>("ScannedTag2").SetLength(12).Value = "1234";
            //            msg.AddField<StringParameter>("ScannedTag3").SetLength(12).Value = "12";
            //            msg.AddField<UInt16Parameter>("TypeScannedTag1").Value =1;
            //            msg.AddField<UInt16Parameter>("TypeScannedTag2").Value = 1;
            //            msg.AddField<UInt16Parameter>("TypeScannedTag3").Value =1;
            //            msg.AddField<UInt16Parameter>("Reserved1");
            //            msg.AddField<UInt32Parameter>("OperatorId");

            //            msg.AddField<UInt16Parameter>("BestHead");
            //            msg.AddField<UInt16Parameter>("HeadsRead");
            //            msg.AddField<UInt16Parameter>("ReadQuality");

            //            msg.AddField<UInt16Parameter>("HeadStatus");
            //            msg.AddField<UInt16Parameter>("PC1Status");
            //            msg.AddField<UInt16Parameter>("PC2Status");
                    
            //            msg.AddField<UInt16Parameter>("ScanAngle");
            //            msg.AddField<UInt16Parameter>("UnhitLength");
            //            msg.AddField<UInt16Parameter>("UnitSeperation");
            //            msg.AddField<UInt16Parameter>("LabelPosition");

            //            msg.AddField<UInt16Parameter>("ReadStatus");
            //            msg.AddField<UInt16Parameter>("Reservesd");
            //        }
            //        else
            //        {

            //            msg.AddField<StringParameter>("Barcode").Length = 12;
            //            msg.AddField<UInt16Parameter>("AssignedSortLocation");
            //            msg.AddField<UInt16Parameter>("StorageSortLocation");
            //            msg.AddField<UInt16Parameter>("SortReason");
            //            msg.AddField<CharParameter>("cTargetScreeningLevel");
            //            msg.AddField<CharParameter>("cScreeningDecision");
            //            msg.AddField<CharParameter>("cLastScreeningLevel");
            //            msg.AddField<CharParameter>("cLastScreeningResult");
            //            msg.AddField<UInt16Parameter>("wReserved1");
            //            msg.AddField<StringParameter>("Carrier").Length = 4;
            //            msg.AddField<UInt16Parameter>("wFlightNumber");
            //            msg.AddField<CharParameter>("cSuffix");
            //            msg.AddField<ByteParameter>("ucReserved1");
            //            msg.AddField<UInt32Parameter>("dwScheduledDate");
            //            msg.AddField<UInt32Parameter>("dwEstimatedDate");
            //            msg.AddField<UInt16Parameter>("wEstimatedTime");
            //            //DEVMSGHEADER    Header;
            //            //PDB_BARCODE     bcIDTag;                // bag's preferred unique tracking identifier (e.g. barcode or dummy barcode)
            //            //WORD            wAssignedSortLocation;  // sort decision assigned destination sort location (lateral)
            //            //WORD            wStorageSortLocation;   // storage decision assigned temporary storage sort location
            //            //WORD            wSortReason;            // sort decision sort reason (TSortReason) - reason code for sort location assignment
            //            //char            cTargetScreeningLevel;  // screening decision target level (ASCII '1','2','3',etc)
            //            //char            cScreeningDecision;     // screening decision ('S' screen, 'T' screen as threat, 'D' do not screen)
            //            //char            cLastScreeningLevel;    // (ASCII '1', '2', '3',etc)
            //            //char            cLastScreeningResult;   // ASCII 'C' cleared, 'R' rejected, 'N' no-decision/timeout
            //            //WORD            wReserved1;             // padding for Allen-Bradley PLC to pack fields into 4-byte DINTs
            //            //char            Carrier[4];             // bag's flight carrier abbreviation ("NZ","QF",etc) if known - pad right with spaces or NUL
            //            //WORD            wFlightNumber;          // bag's flight number (1-9999) if known
            //            //char            cSuffix;                // bag's flight number suffix (ASCII 'A'-'Z' or NUL) if known
            //            //BYTE            ucReserved1;            // padding for Allen-Bradley PLC to pack fields into 4-byte DINTs
            //            //DWORD           dwScheduledDate;        // bag's flight scheduled date (YYYYMMDD) if known (e.g. 20130429)
            //            //DWORD           dwEstimatedDate;        // bag's flight estimated date (YYYYMMDD) if known (e.g. 20130429)
            //            //WORD            wEstimatedTime;      

            //        }
            //        break;
            //    case 126:
            //            msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "Bag Drop Sort Decision";
            //            if (_dir == Direction.Send)
            //            {

            //                //DEVMSGHEADER    Header;
            //                //PDB_BARCODE     bcIDTag;            // IATA bag tag - license plate
            //                //char            Carrier[4];         // 2 or 3 letter Carrier abbreviation (pad right with ACII NUL for unused characters)
            //                //WORD            wFlightNumber;      // Flight number (1-9999)
            //                //char            cFlightSuffix;      // Flight number suffix (NUL if not set)
            //                //char            cBagClass;          // Bag class (use 'Y' if not set (NUL))
            //                //DWORD           dwFlightDate;       // (formatted unsigned integer, format YYYYMMDD) Scheduled flight date (use today if not set(NULL))
            //                //char            Destination[4];     // 3 or 4 letter Bag final destination code (pad right with ASCII spaces or NUL if not set)

            //                msg.AddField<StringParameter>("Barcode").Length = 12;
            //                msg.AddField<StringParameter>("Carrier").Length = 4;
            //                msg.AddField<UInt16Parameter>("wFlightNumber");
            //                msg.AddField<CharParameter>("cSuffix");
            //                msg.AddField<CharParameter>("cBagClass");
            //                msg.AddField<UInt32Parameter>("dwFlightDate");
            //                msg.AddField<StringParameter>("Destination").Length = 4;
            //            }
            //            else
            //            {
            //                msg.AddField<StringParameter>("Barcode").Length = 12;
            //                msg.AddField<UInt16Parameter>("AssignedSortLocation");
            //                msg.AddField<UInt16Parameter>("StorageSortLocation");
            //                msg.AddField<UInt16Parameter>("SortReason");
            //                msg.AddField<CharParameter>("cTargetScreeningLevel");
            //                msg.AddField<CharParameter>("cScreeningDecision");
            //                msg.AddField<CharParameter>("cLastScreeningLevel");
            //                msg.AddField<CharParameter>("cLastScreeningResult");
            //                msg.AddField<UInt16Parameter>("wReserved1");
            //                msg.AddField<StringParameter>("Carrier").Length = 4;
            //                msg.AddField<UInt16Parameter>("wFlightNumber");
            //                msg.AddField<CharParameter>("cSuffix");
            //                msg.AddField<ByteParameter>("ucReserved1");
            //                msg.AddField<UInt32Parameter>("dwScheduledDate");
            //                msg.AddField<UInt32Parameter>("dwEstimatedDate");
            //                msg.AddField<UInt16Parameter>("wEstimatedTime");


            //            }

            //        break;
            //    case 111: //ME_BAG_DECISION_T2 from PLC to AAA MES
            //         msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "Me Bag Decision AAA";
            //            if (_dir == Direction.Send)
            //            {
            //                msg.AddField<StringParameter>("Barcode").SetLength(12).Padding = Padding.SPACE;
            //                msg.AddField<UInt16Parameter>("Weight");
            //            }
            //            else
            //            {
            //                msg.AddField<StringParameter>("Barcode").SetLength(12).Padding = Padding.SPACE;
            //                msg.AddField<UInt16Parameter>("Result");
            //                msg.AddField<UInt16Parameter>("Flags");
            //                msg.AddField<UInt16Parameter>("Weight");
            //            }
            //       break;
            //    case 91: //ME_BAG_DECISION
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "Me Bag Decision";
            //            msg.AddField<StringParameter>("Barcode").SetLength(12).Padding = Padding.SPACE;
                   
            //       break;
            //    case 134:
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "Sorter Item Info";
            //            msg.AddField<StringParameter>("Barcode").SetLength(12).Padding = Padding.SPACE;
            //            msg.AddField<UInt16Parameter>("SorterId");
            //            msg.AddField<UInt16Parameter>("SorterTrayId");
            //            msg.AddField<UInt32Parameter>("FlightDate");
            //            msg.AddField<UInt32Parameter>("FlightETD");
            //            msg.AddField<StringParameter>("Carrier").SetLength(4).Padding = Padding.SPACE;
            //            msg.AddField<UInt16Parameter>("FlightNo");
            //            msg.AddField<UInt16Parameter>("SortLocation");
            //            msg.AddField<CharParameter>("FlightSuffix");
            //            msg.AddField<ByteArrayParameter>("Screening Res").Length = 4;
            //            msg.AddField<ByteParameter>("Tracking Status");
            //            msg.AddField<UInt16Parameter>("Recirc count");
            //            msg.AddField<UInt16Parameter>("Update Type");
            //            msg.AddField<UInt16Parameter>("Trailer");
            //       break;
            //    case 53 : //Weight reconciliation request (Air Nz )
            //            //DEVMSGHEADER	Header;
            //            //PDB_BARCODE		Barcode;		//IATA bag tag	
            //            //WORD			wBagWeight;		//Bag weight (Grams)
            //            //DWORD			dwReserved;		//Should be initialised to 0's by PLC
            //            msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "KF Weight Reconciliation Request";
            //            if (_dir == Direction.Send)
            //            {
            //                msg.AddField<StringParameter>("Barcode").SetLength(12,10).Padding = Padding.SPACE;
            //                msg.AddField<UInt16Parameter>("Weight");
            //                msg.AddField<UInt32Parameter>("Reserved");
            //            }
            //            else
            //            {
            //                msg.AddField<UInt16Parameter>("Result");
            //                msg.AddField<UInt16Parameter>("Process Flags");
            //            }
            //       break;
            //    case 94 :
            //            msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "KF ME Reconciliation Request";

            //            msg.AddField<UInt16Parameter>("Reason Id");
            //            msg.AddField<StringParameter>("Barcode").SetLength(12,10).Padding = Padding.SPACE;
            //            msg.AddField<StringParameter>("Sec Barcode").SetLength(12).Padding = Padding.SPACE;
            //            msg.AddField<UInt16Parameter>("Weight");
            //            msg.AddField<UInt32Parameter>("Weight Override");
            //            msg.AddField<ByteParameter>("Flags");
            //            msg.AddField<UInt32Parameter>("Operator Id");
            //            msg.AddField<CharParameter>("MES Code");
            //            msg.AddField<UInt16Parameter>("Flight Ac Time");
            //            msg.AddField<UInt32Parameter>("Flight Acc Overide");
            //       break;
            //    case 52 : //Fallback request (to reconcile dummy tag to a real barcode
            //        msg = new SACMessage(_hdr as SACMessageHeader);
            //            msg.Header.Name = "KF Fallback Request";

            //            if (_dir == Direction.Send)
            //            {
            //                msg.AddField<StringParameter>("Barcode").SetLength(12,10).Padding = Padding.SPACE;
            //                msg.AddField<StringParameter>("Dummy Tag").SetLength(12).Padding = Padding.SPACE;
            //                msg.AddField<UInt32Parameter>("Reserved");
            //            }
            //            else
            //            {
            //                msg.AddField<UInt16Parameter>("Reconciliation Res");
            //                msg.AddField<UInt16Parameter>("Reconciliation Flags");
            //                msg.AddField<UInt16Parameter>("Reserved");
            //            }
            //       break;
            //}
            //if (msg != null)
            //{
            //    msg.SetLenght();
            //}
            return msg;
        }
    }
}
