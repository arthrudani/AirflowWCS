using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace AirflowDeviceSim
{

    public partial class StatsForm : Form
    {
        public uint BytesSent
        {
            set
            {
                labelSent.Text = value.ToString();
            }
        }

        public uint BytesReceived
        {
            set
            {
                labelReceived.Text = value.ToString();
            }
        }

        public StatsForm()
        {
            InitializeComponent();
        }
    }
}
