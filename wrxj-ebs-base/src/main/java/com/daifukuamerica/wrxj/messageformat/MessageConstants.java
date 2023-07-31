package com.daifukuamerica.wrxj.messageformat;

/**
 * Title:        Java Development
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      SKDC
 * @author
 * @version 1.0
 */

public interface MessageConstants
{
// Each device type will need an ID

  final int AGCMESSAGETYPE = 1;       // AGC device
  final int RFMESSAGETYPE = 2;        // RF Device
  final int PTLMESSAGETYPE = 3;       // Put To Light Message Type
  final int SCHEDULERMESSAGETYPE = 4; // Allocator Message Type
  final int CONVEYORMESSAGETYPE = 5;  // Conveyor Message Type
  final int ADSIMESSAGETYPE = 6;      // ADSI Connect Ship Message Type
  final int LNINFORMATION = 30;       // Length of information field.

/*============================================================================
 *           Constants for "Transport Classification" in message 12.
 *============================================================================*/
  final int RETRIEVAL_MOVE = 2;
  final int SHELF_TO_SHELF_MOVE = 5;

/*============================================================================
 *       Constants for retrieval Category (priority) field in message 12.
 *============================================================================*/
  final int URGENT_RETRIEVAL = 1;
  final int PLANNED_RETRIEVAL = 2;
  final int EMPTY_LOCATION_CHECK = 9;

/*============================================================================
 *       Constants for "Retrieval Command Detail" field in message 12.
 *============================================================================*/
  final int INVENTORY_CHECK   = 0;
  /**
   * Normally used when retrieving to a output station where the SRC is no longer
   * required to track data.
   */
  final int UNIT_RETRIEVAL    = 1;
  /**
   * Normally used in the Retrieve Command when retrieving to a PD stand or Reversible
   * if the intent is <u>not</u> to have the SRC/AGC delete tracking.  
   * <b>Note:</b> the AGC/SRC can be configured for Load Removal (it will delete
   * tracking when operator pushes completion button and removes load -- LP = 0)
   * even if this flag is set!
   */
  final int PICKING_RETRIEVAL = 2;
  final int ADDING_RETRIEVAL  = 3;

/*============================================================================
 *           Constants for "Reinputting flag" in messsage 12.
 *============================================================================*/
  /**
   * If this flag is used in the retrieve command, the SRC will require Wrx
   * to tell it where to restore the load.  This is the default behaviour.
   */
  final int NO_REINPUT = 0;
  /**
   * If this flag is used in the retrieval command, the SRC will return the
   * retrieved load back to the same location in the rack (it remembers the
   * bin location) when the operator pushes the completion button.  There is
   * no need for Wrx to actually issue a Store Command in this case.
   */
  final int REINPUT_SAME_LOC = 1;
}