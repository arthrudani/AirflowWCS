using System;
using System.Collections.Generic;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    public abstract class BaseParameter  
    {
        public virtual int Length
        {
            get;
            set;
        }

        public virtual int DataEntryLength
        {
            get;
            set;
        }


        public bool ReadOnly
        {
            get;
            set;
        }

        //public virtual String Id { get; set; }
        public virtual String Name { get; set; }

       protected BaseParameter()
        {
			
        }
        //offset has to be modified in side this function. So it is passed as a referance
        public abstract  void Write(Byte[] buffer, int offset, bool netByteOrder);
        public abstract void Read(Byte[] buffer, int offset, bool netByteOrder);
        public virtual void Write(Byte[][] buffer)
        {
            
        }

        public abstract void FromString(String _str);
     }
}
