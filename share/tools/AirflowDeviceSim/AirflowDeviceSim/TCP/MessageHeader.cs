using System;
using System.Linq;
using System.Collections.Generic;
using System.Text;

namespace AirflowDeviceSim.TCP
{
	public  class MessageHeader : MessageBase
	{
        public String Name
        {
            get;
            set;
        }
        
        public DateTime TimeStamp
        {
            get;
            set;
        }

        public virtual void setMessageType(ushort msgType)
        {
        }

        public override string ToString()
        {
            return " [" + Name +"] " +  base.ToString();
        }
	}
}
