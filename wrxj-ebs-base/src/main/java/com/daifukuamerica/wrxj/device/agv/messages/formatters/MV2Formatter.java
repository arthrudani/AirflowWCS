package com.daifukuamerica.wrxj.device.agv.messages.formatters;

import com.daifukuamerica.wrxj.device.agv.AGVDBInterface;
import com.daifukuamerica.wrxj.device.agv.AGVException;
import com.daifukuamerica.wrxj.device.agv.messages.AGVMessageFormatterException;

/**
 * <b>Message direction: WRx-J -----> CMS.</b>  Class to format the MV2 command
 * that moves two load moves to two destination locations.
 *
 * @author A.D.
 * @since  12-May-2009
 */
public class MV2Formatter extends MOVFormatter
{

  public MV2Formatter()
  {
    super();
  }

/*----------------------------------------------------------------------------
                           Interface Methods
  ----------------------------------------------------------------------------*/
  @Override
  public String format(int inSequenceNumber, AGVDBInterface ipDBInterface)
         throws AGVMessageFormatterException, AGVException
  {
    //TODO
    
//    mapData = ipDBInterface.getData(inSequenceNumber);
    String vsMesg = "";
//
//    try
//    {
//      FieldTemplate[] vapFormatTemplate = new FieldTemplate[]
//      {
//        new FieldTemplate(Integer.valueOf(inSequenceNumber),
//                                  AGVMessageConstants.SERIAL_NUMBER_LEN),
//        new FieldTemplate(AGVMessageNameEnum.MV2_REQUEST,
//                                  AGVMessageConstants.MESSAGEID_LEN),
//        new FieldTemplate(msRequestID, AGVMessageConstants.REQUESTID_LEN),
//        new FieldTemplate(msMoveSequence, AGVMessageConstants.MOVE_SEQUENCE_LEN),
//        new FieldTemplate(msPickLocation, AGVMessageConstants.LOCATION_LEN),
//        new FieldTemplate(Integer.valueOf(mnPickLocationHeight),
//                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
//        new FieldTemplate(Integer.valueOf(mnPickLocationDepth),
//                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
//        new FieldTemplate(msDropLocation, AGVMessageConstants.LOCATION_LEN),
//        new FieldTemplate(Integer.valueOf(mnDropLocationHeight),
//                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
//        new FieldTemplate(Integer.valueOf(mnDropLocationDepth),
//                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
//        new FieldTemplate(msLoadID, AGVMessageConstants.LOAD_LEN),
//
//        new FieldTemplate(msSecondPickLocation,
//                                  AGVMessageConstants.LOCATION_LEN),
//        new FieldTemplate(Integer.valueOf(mnSecondPickLocationHeight),
//                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
//        new FieldTemplate(Integer.valueOf(mnSecondPickLocationDepth),
//                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
//        new FieldTemplate(msSecondDropLocation,
//                                  AGVMessageConstants.LOCATION_LEN),
//        new FieldTemplate(Integer.valueOf(mnSecondDropLocationHeight),
//                                  AGVMessageConstants.LOCATION_HEIGHT_LEN),
//        new FieldTemplate(Integer.valueOf(mnSecondDropLocationDepth),
//                                  AGVMessageConstants.LOCATION_DEPTH_LEN),
//        new FieldTemplate(msSecondLoadID, AGVMessageConstants.LOAD_LEN)
//      };
//
//      vsMesg = format( vapFormatTemplate);
//    }
//    catch(UnsupportedOperationException exc)
//    {
//      throw new AGVMessageFormatterException("Error formatting " +
//                                 AGVMessageNameEnum.MV2_REQUEST.getValue() +
//                                 " message. " + exc.getMessage());
//    }
//
    return(vsMesg);
  }

  @Override
  public void postSendProcessing(AGVDBInterface ipDBInterface) throws AGVException
  {
  }
}
