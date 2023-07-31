using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.TCP
{
    abstract public class Parameter<T> : BaseParameter
    {
        virtual public T Value { get; set; }
        public Parameter()
        {
        }

        public override string ToString()
        {
            return Name + " [" + Value.ToString() + "] ";
        }
    }
}
