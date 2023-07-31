using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using AirflowDeviceSim.TCP;

namespace AirflowDeviceSim
{
    class SmartflowMessage : Message
    {
        public SmartflowMessage(SmartflowHeader _hdr)
            : base(_hdr)
        {
            
        }

        override public void WrapUp()
        {
            Header.GetField<UInt16Parameter>("Length").Value = (UInt16)(Length + Header.Length); 
        }
    }
}
