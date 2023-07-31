using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.ComponentModel;


namespace AirflowDeviceSim.TCP
{
	public  class Message : MessageBase
	{
		

        [Browsable(false)]
        public MessageHeader Header
        {
            get;
            protected set;

        }

        public String Name
        {
            get
            {
                return Header.Name;
            }
        }

        public int ByteCount
        {
            get;
            set;
        }

        public Message(MessageHeader _header)
        {
            Header = _header;
        }

        public override string ToString()
        {
            return Header.ToString() + base.ToString() + ' ' +( Header.Length + Length ).ToString() + " bytes.";
        }
	
 	}
}
