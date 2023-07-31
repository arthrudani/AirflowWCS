using System;
using System.Collections.Generic;
using System.Text;
using PostcodeGUI.TCP;

namespace PostcodeGUI
{
	public class MessageProvider : IMessageProvider 
	{
		public IMessage GetMessage( IMessageHeader iHdr  )
		{
		   Message msg = Message.Create((MessageHeader)iHdr );
		   return msg;
		}
	}
}
