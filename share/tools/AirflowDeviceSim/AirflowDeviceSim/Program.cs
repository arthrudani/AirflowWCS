using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using BCS.Library;
using BCS.Library.Logging;
using BCS.Library.Logging.Log4Net;


namespace AirflowDeviceSim
{
    static class Program
    {
        static ILogger Logger;
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            try
            {
                Log4NetManager.Initialize();
                Logger = Log4NetManager.GetRootLogger();
                if (Logger == null)
                {
                    Logger = new NullLogger();
                }
                Application.EnableVisualStyles();
                Application.SetCompatibleTextRenderingDefault(false);
                Application.Run(new Form1( Logger));
            }
            catch (Exception ex)
            {
                if (Logger != null)
                {
                    Logger.Error(ex);
                }
            }
        }
    }
}
