namespace PostcodeGUI
{

	public enum Messagetype
	{
		TypeHeader = 0,
		TypeTerminalStatus = 1 ,
		TypePLCStatus = 2 ,
		TypeEvent = 3,
		TypeBuffer = 4,
		TypeTransaction = 5,
		TypeStatusRequest = 6,
		TypePostCodeTablestatus = 7,
		TypeShiftStatus = 8,
		EventRequest = 9,
		HeaderOnly = 10,
		LogRequest = 11,
	}


	public enum MessageIDS : byte
	{
		 Report			= 1,
		 Request		= 2,
		LogRequest		= 3,
	}

/*
	// message types
//------------------
const BYTE gmtStatusReport			= 1;
const BYTE gmtStatusRequest			= 2;

// sub message types
//-------------------
const BYTE smtMiniTermStatus		= 1;
const BYTE smtPLCStatus				= 2;
const BYTE smtAMPMStatus			= 3;
const BYTE smtPostcodeTableStatus	= 4;

const BYTE smtEventPollRequest		= 5;
const BYTE smtEventPollReport		= 6;
const BYTE smtEventReportComplete	= 16;
const BYTE smtEventPushReport		= 7;

const BYTE smtTransPollRequest		= 8;
const BYTE smtTransPollReport		= 9;
const BYTE smtTransReportComplete	= 19;
const BYTE smtTransPushReport		= 10;
*/

	public enum MessageSubIDS : byte
	{
		TerminalStatus		= 1,
		PLCStatus			= 2,
		ShiftStatus			= 3,
		PostCodeTableStatus = 4,

		EventReq			= 5,
		EventReport			= 6 ,
		EventsReportUpdate	= 7 ,
		EventReportComplete = 16,

		TransactionReq		= 8,
		TransactionReport	= 9 ,
		TransactionUpdate   = 10,
		TransactionComplete	= 19 ,


		EventReportLocal	= 50 ,
		LogRequest			= 1,
	}
 }