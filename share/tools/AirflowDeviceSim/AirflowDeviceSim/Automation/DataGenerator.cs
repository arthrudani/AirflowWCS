using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace AirflowDeviceSim
{
    public class DataGenerator
    {
        int CurrentValue;
        public int MinValue { get; set; }
        public int MaxValue { get; set; }
        public int Length { get; set; }
        public String GetNextValue()
        {
            CurrentValue++;
            CurrentValue = Math.Max(Math.Min(MaxValue, CurrentValue), MinValue);
            return CurrentValue.ToString("D" + Length.ToString());
        }
    }
}
