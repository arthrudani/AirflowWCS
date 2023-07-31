using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim.Tools
{
    public class Util
    {


        public static string GetArrayAsString(UInt16[] val)
        {
            String s = "[";
            int index = 1;
            foreach (int b in val)
            {
                if (index > 1)
                    s += ',';

                s += b.ToString();

                index++;
            }
            return s + "]";
        }
    }
}
