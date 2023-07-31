using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Net;
using System.Windows.Forms;
using System.IO;
using System.Reflection;
using System.Xml.Serialization;
using BCS.Library.Logging;
using Tcp = AirflowDeviceSim.TCP;
using AirflowDeviceSim.Messages;
using System.Configuration;


namespace AirflowDeviceSim
{

    public partial class Form1 : Form
    {
        bool UseMsgWrapping = true;
        String SettingsFilePath = string.Empty;
        String SettingsFilePathInstalled = string.Empty;

        ILogger Logger;
        AppSettings AppSettings;
        bool Initializing;

        Tcp.TCPIPInterfaceManager ClientManager;
        Tcp.IMessageFactory Factory;
        Tcp.TCPClientInterface Client;
        DateTime[] Message1ScheduledTimes = new DateTime[3];

        int[] MessageIntervals = new int[3] { 10000, 10000, 10000 };
        Dictionary<String, DataGenerator> DataGenerators;
        List<Device> DeviceList;

        volatile uint BytesSent;
        volatile uint BytesReceived;

        public Form1(ILogger _logger)
        {
            Logger = _logger;
            InitializeComponent();
            ClientManager = new Tcp.TCPIPInterfaceManager();
            //Factory = new MessageFactory();
            //Factory = new MessageFactorySmartflow();
            //Factory = new MessageFactoryForStressRest();
            DataGenerators = new Dictionary<string, DataGenerator>();
            DataGenerators["Barcode"] = new DataGenerator { MaxValue = 5000, MinValue = 0, Length = 10 };
            GetConfigurationValue();
 
        }

        public void GetConfigurationValue()
        {
            var airflowDeviceSimulatorFile = ConfigurationManager.AppSettings["AirflowDeviceSimulatorFile"];
            SettingsFilePath = Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.CommonApplicationData), airflowDeviceSimulatorFile);

            var airflowDeviceSimulatorInstalled = ConfigurationManager.AppSettings["AirflowDeviceSimulatorInstalled"];
            SettingsFilePathInstalled = Path.Combine(System.Environment.GetFolderPath(System.Environment.SpecialFolder.CommonApplicationData), airflowDeviceSimulatorInstalled);
            Console.WriteLine(string.Format("AirflowDeviceSimulatorFile is load from : '{0}' and AirflowDeviceSimulatorInstalled is load from : '{1}' ", SettingsFilePath, SettingsFilePathInstalled));
        }

        public static string AssemblyDirectory
        {
            get
            {
                string codeBase = Assembly.GetExecutingAssembly().CodeBase;
                UriBuilder uri = new UriBuilder(codeBase);
                string path = Uri.UnescapeDataString(uri.Path);
                return Path.GetDirectoryName(path);
            }
        }
        /// <summary>
        /// Adds messages available for sending to HLC/SAC
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void OnFormLoad(object sender, EventArgs e)
        {
            Initializing = true;
            try
            {
                Assembly a = Assembly.GetExecutingAssembly();
                this.Text = this.Text + " (" + a.GetName().Version.ToString() + ')';

                //Continue otherwise;
                int selectedindex = 0;
                bool loaded = loadSettings();
                if (loaded)
                {
                    foreach (Factory fct in AppSettings.Factories)
                    {

                        if (fct.SelectDefault)
                        {
                            //set message factory
                            if (fct.Name == "MF_For_Stress_Test")
                            {
                                Factory = new MessageFactoryForStressRest();
                            }
                            else if (fct.Name == "Smartflow")
                            {
                                Factory = new MessageFactorySmartflow();
                            }
                            else if (fct.Name == "PLCMessageFactory")
                            {
                                Factory = new MessageFactoryPLC();
                                DeviceList= GetDeviceData("PLC");
                            }
                            else if (fct.Name == "SACMessageFactory")
                            {
                                Factory = new MessageFactorySAC();
                                DeviceList = GetDeviceData("SAC");
                            }
                            else
                            {
                                //Airflow Air Nz
                                Factory = new MessageFactory();
                            }
                            AppSettings.SelectedFactory = selectedindex;
                        }
                        selectedindex++;
                    }

                    //Poulate other Data 
                    PopulateFormData(DeviceList);
                }
                else
                {
                    MessageBox.Show("Failed to load the settings files:" + SettingsFilePath + " \n - " + SettingsFilePathInstalled);

                    this.Close();
                }
            }
            catch (Exception ex)
            {
                Logger.Error("Starting application", ex);
                MessageBox.Show(ex.Message);
            }
            Initializing = false;

        }

        private List<Device> GetDeviceData(string type)
        {
            DeviceList = new List<Device>();
            foreach (Device dev in AppSettings.Devices)
            {
                if (dev.Type == type) {
                    DeviceList.Add(dev);
                }
            }
            return DeviceList;
        }

        private void PopulateFormData(List<Device> deviceList)
        {
            //Poulate device messages
            List<AirflowDeviceSim.TCP.Message> supportedMessages1 = new List<Tcp.Message>();
            List<AirflowDeviceSim.TCP.Message> supportedMessages2 = new List<Tcp.Message>();
            List<AirflowDeviceSim.TCP.Message> supportedMessages3 = new List<Tcp.Message>();

            foreach (DeviceMessage dm in AppSettings.DeviceMessages)
            {
                Tcp.MessageHeader header = Factory.AddMessageDefinition(dm.Name, dm.Id);
                if (header != null)
                {
                    supportedMessages1.Add(Factory.CreateMessage(header, Tcp.Direction.Send));
                    supportedMessages2.Add(Factory.CreateMessage(header, Tcp.Direction.Send));
                    supportedMessages3.Add(Factory.CreateMessage(header, Tcp.Direction.Send));
                }
            }


            LoadMessageState(AppSettings.MessageState1, supportedMessages1);
            LoadMessageState(AppSettings.MessageState2, supportedMessages2);
            LoadMessageState(AppSettings.MessageState3, supportedMessages3);

            comboBoxMessages.DataSource = supportedMessages1;
            comboBoxMessages2.DataSource = supportedMessages2;
            comboBoxMessage3.DataSource = supportedMessages3;
            comboBoxMessages.DisplayMember = "Name";
            comboBoxMessages2.DisplayMember = "Name";
            comboBoxMessage3.DisplayMember = "Name";

            comboBoxServer.DataSource = AppSettings.Servers;
            comboBoxDevice.DataSource = deviceList;
            comboBoxFactory.DataSource = AppSettings.Factories;

            SetSelection(comboBoxServer, AppSettings.SelectedServer);
            SetSelection(comboBoxDevice, AppSettings.SelectedDevice);
            SetSelection(comboBoxFactory, AppSettings.SelectedFactory);

            SetSelection(comboBoxMessages, AppSettings.SelectedMessage1);
            SetSelection(comboBoxMessages2, AppSettings.SelectedMessage2);
            SetSelection(comboBoxMessage3, AppSettings.SelectedMessage3);

            MessageIntervals[0] = AppSettings.Message1Interval;
            MessageIntervals[1] = AppSettings.Message2Interval;
            MessageIntervals[2] = AppSettings.Message2Interval;

            textBoxTimer1.Text = MessageIntervals[0].ToString();
            textBoxTimer2.Text = MessageIntervals[1].ToString();
            textBoxTimer3.Text = MessageIntervals[2].ToString();

            comboBoxMessages.Select();
        }


        private void ReloadFom(Factory fct)
        {
            try
            {
                if (fct != null && !Initializing)
                {
                    //set message factory
                    if (fct.Name == "MF_For_Stress_Test")
                    {
                        Factory = new MessageFactoryForStressRest();
                    }
                    else if (fct.Name == "Smartflow")
                    {
                        Factory = new MessageFactorySmartflow();
                    }
                    else if (fct.Name == "PLCMessageFactory")
                    {
                        Factory = new MessageFactoryPLC();
                        DeviceList = GetDeviceData("PLC");
                    }
                    else if (fct.Name == "SACMessageFactory")
                    {
                        Factory = new MessageFactorySAC();
                        DeviceList = GetDeviceData("SAC");
                    }
                    else
                    {
                        //Airflow Air Nz
                        Factory = new MessageFactory();
                    }
                    AppSettings.SelectedFactory = comboBoxFactory.SelectedIndex;

                    //Poulate other Data 
                    PopulateFormData(DeviceList);
                }               
            }
            catch (Exception ex)
            {
                Logger.Error("Reloading application Data", ex);
                MessageBox.Show(ex.Message);
            }
        }

        private bool loadSettings()
        {
            bool loaded = true;
            if (!File.Exists(SettingsFilePath) || !File.Exists(SettingsFilePathInstalled))
            {


                return false;
            }

            AppSettings installed = null;
            if (File.Exists(SettingsFilePath))
            {
                AppSettings = LoadAppSettings(SettingsFilePath);
            }
            if (File.Exists(SettingsFilePathInstalled))
            {
                installed = LoadAppSettings(SettingsFilePathInstalled);
            }
            bool save = false;
            if (installed != null && AppSettings != null)
            {
                save = AppSettings.MergeFrom(installed);
            }
            else if (installed != null)
            {
                AppSettings = installed;
                save = true;
            }
            else if (AppSettings == null)
            {
                AppSettings = new AirflowDeviceSim.AppSettings();
                AppSettings.Prepopulate();
                save = true;
            }

            if (save)
            {
                SaveAppSettings(AppSettings, SettingsFilePath);
            }

            return loaded;
        }


        private void OnFormClosing(object sender, FormClosingEventArgs e)
        {

            try
            {
                if (AppSettings.Message1Interval != MessageIntervals[0])
                {
                    AppSettings.Message1Interval = MessageIntervals[0];
                }

                if (AppSettings.Message2Interval != MessageIntervals[1])
                {
                    AppSettings.Message2Interval = MessageIntervals[1];
                }

                if (AppSettings.Message3Interval != MessageIntervals[2])
                {
                    AppSettings.Message3Interval = MessageIntervals[2];
                }

                SaveMessageState(comboBoxMessages, AppSettings.MessageState1);
                SaveMessageState(comboBoxMessages2, AppSettings.MessageState2);
                SaveMessageState(comboBoxMessage3, AppSettings.MessageState3);
                SaveAppSettings(AppSettings, SettingsFilePath);
            }
            catch (Exception ex)
            {
                Logger.Error("Closing application", ex);
            }
        }

        void SetSelection(ComboBox cmb, int idx)
        {
            if (idx < cmb.Items.Count)
            {
                cmb.SelectedIndex = idx;
            }
        }

        void LoadMessageState(IEnumerable<MessageState> _state, IEnumerable<Tcp.Message> _messages)
        {
            foreach (MessageState ms in _state)
            {
                Tcp.Message msg = _messages.FirstOrDefault(m => m.Name == ms.Name);
                if (msg != null)
                {
                    try
                    {
                        foreach (ParamState ps in ms.Parameters)
                        {
                            if (ps.Value != null)
                            {
                                Tcp.BaseParameter p = msg[ps.Name];
                                if (p != null)
                                {
                                    Type t = p.GetType();
                                    var prop = t.GetProperty("Value");
                                    prop.SetValue(p, ps.Value, null);
                                }
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        Logger.Error("Loading message state", ex);
                    }
                }
            }
        }


        void SaveMessageState(ComboBox cmb, List<MessageState> stateList)
        {
            IEnumerable<Tcp.Message> messages = cmb.DataSource as IEnumerable<Tcp.Message>;
            if (messages != null)
            {
                try
                {
                    foreach (Tcp.Message m in messages)
                    {
                        MessageState state = new MessageState() { Name = m.Name };
                        foreach (Tcp.BaseParameter p in m.Parameters)
                        {
                            Type t = p.GetType();
                            var prop = t.GetProperty("Value");

                            var Value = prop.GetValue(p, null);
                            //row.Cells[1].ValueType = v.PropertyType;
                            state.Parameters.Add(new ParamState { Name = p.Name, Value = Value });
                        }
                        stateList.Add(state);
                    }
                }
                catch (Exception ex)
                {
                    Logger.Error("Saving state", ex);
                }
            }
        }


        AppSettings LoadAppSettings(String _path)
        {
            AppSettings ret = null;
            XmlSerializer xs = null;
            StreamReader reader = null;

            try
            {
                xs = new XmlSerializer(typeof(AppSettings));
                reader = new StreamReader(_path);
                ret = (AppSettings)xs.Deserialize(reader);
            }
            catch (Exception ex)
            {
                Logger.Error("Load settings", ex);
                //ShowErrorMessageBox("Failed to load settings. Default values are used",ex.Message);    
            }
            finally
            {
                if (reader != null)
                {
                    reader.Close();
                }
            }
            return ret;
        }


        bool SaveAppSettings(AppSettings _toSave, String _path)
        {
            bool ok = false;
            XmlSerializer xs = null;
            StreamWriter writer = null;
            try
            {
                xs = new XmlSerializer(typeof(AppSettings));
                writer = new StreamWriter(_path);
                xs.Serialize(writer, _toSave);
                ok = true;
            }
            catch (Exception ex)
            {
                ShowError("Save settings", ex);
            }
            finally
            {
                if (writer != null)
                {
                    writer.Flush();
                    writer.Close();
                }
            }
            return ok;
        }

        [System.Runtime.InteropServices.StructLayout(System.Runtime.InteropServices.LayoutKind.Sequential, Pack = 1)]
        struct Header
        {
            byte MessageTypeParam;
            byte DeviceId;
            UInt16 LocId;
            UInt16 DeviceRef;
            UInt16 Length;
        }



        private void buttonConnect_Click(object sender, EventArgs e)
        {
            try
            {
                if (Client == null)
                {
                    IPAddress ip = null;
                    if (!IPAddress.TryParse(textBoxServer.Text, out ip))
                    {
                        IPAddress[] ips = Dns.GetHostAddresses(textBoxServer.Text);
                        if (ips != null && ips.Length > 0)
                        {
                            ip = ips.FirstOrDefault(i => i.AddressFamily == System.Net.Sockets.AddressFamily.InterNetwork);
                            if (ip == null)
                            {
                                ip = ips.FirstOrDefault(i => i.AddressFamily == System.Net.Sockets.AddressFamily.InterNetworkV6);
                            }
                        }
                    }
                    if (ip == null)
                    {
                        throw new Exception("Invalid host name or ip address. [" + textBoxServer.Text + ']');
                    }

                    int port = int.Parse(textBoxPort.Text);

                    if (radioButtonClient.Checked)
                    {
                        Client = ClientManager.AddClient(ip, port, Factory);
                    }
                    else
                    {
                        Client = ClientManager.AddServer(ip, port, Factory);
                    }
                    Client.StatusCallback = TCPConnectionEvent;
                    Client.MessageReceived = TCPClientMessageReceived;
                    Client.MessageSent = TCPMessageSent;
                    Client.ReportError = ShowTcpError;
                    Client.NetworkByteOrder = checkBoxNetByteOrder.Checked;
                    //check if need to add STX and ETX to the message
                    //Client.AddWrapper = checkBoxWrapping.Checked;   
                    ClientManager.StartClients();
                    labelStatus.Text = "Connecting";
                    buttonConnect.Text = "Abort";
                }
                else
                {
                    Client.Stop();
                    ClientManager.RemoveClient(Client);
                    Client.Dispose();
                    Client = null;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
                Logger.Error(ex);
            }
        }



        void TCPConnectionEvent(Tcp.TCPConnectionState _state)
        {
            labelStatus.BeginInvoke(new Tcp.TCPConnectionEvent(TCPConnectionStateUIUpdater), _state);
        }

        void TCPClientMessageReceived(Object _sender)
        {
            listViewTransactions.BeginInvoke(new Action(TCPClientMessageReceivedUIUpdater));
        }


        void TCPMessageSent(AirflowDeviceSim.TCP.Message msg)
        {
            BytesSent += (uint)msg.ByteCount;
            listViewTransactions.BeginInvoke(new Tcp.MessageSentEvent(TCPClientMessageSentUIUpdater), msg);
        }

        void ShowTcpError(Exception _ex)
        {
            Logger.Error(_ex);
            listViewUserMessages.BeginInvoke(new Action<String, Exception>(ReportErrorUIUpdater), null, _ex);
        }

        void ShowError(String _header, Exception _ex)
        {
            Logger.Error(_header, _ex);
            ReportErrorUIUpdater(_header, _ex);
        }

        void ReportErrorUIUpdater(String _header, Exception _ex)
        {
            ListViewItem lvi = new ListViewItem();
            lvi.SubItems.Add(DateTime.Now.ToString("HH:mm:ss.fff"));
            lvi.SubItems.Add(_ex.InnerException != null ? _ex.Message + " (" + _ex.InnerException.Message : _ex.Message + ')');
            if (!String.IsNullOrEmpty(_header))
            {
                lvi.SubItems[1].Text = _header + ": " + lvi.SubItems[1].Text;
            }
            lvi.ImageIndex = 2;
            listViewUserMessages.Items.Add(lvi);
        }

        void TCPClientMessageReceivedUIUpdater()
        {
            AirflowDeviceSim.TCP.Message msg = null;
            while ((msg = Client.GetMessage()) != null)
            {
                BytesReceived += (uint)msg.ByteCount;
                if (checkBoxHide1.Checked == false || comboBoxMessages.SelectedItem == null || ((Tcp.Message)comboBoxMessages.SelectedItem).Name != msg.Name)
                {
                    AddLListItem(msg, 1);
                }
            }
        }

        void AddLListItem(AirflowDeviceSim.TCP.Message _message, int _image)
        {
            if (listViewTransactions.Items.Count >= 1000)
            {
                listViewTransactions.Items.RemoveAt(0);
            }

            ListViewItem lvi = new ListViewItem();
            lvi.SubItems.Add(_message.Header.TimeStamp != DateTime.MinValue ? _message.Header.TimeStamp.ToString("yyyy-MM-dd HH:mm:ss.fff") : "");
            lvi.SubItems.Add(_message.ToString());
            lvi.ImageIndex = _image;
            listViewTransactions.Items.Add(lvi);
        }

        void TCPClientMessageSentUIUpdater(AirflowDeviceSim.TCP.Message msg)
        {
            if (checkBoxHide1.Checked == false || comboBoxMessages.SelectedItem == null || ((Tcp.Message)comboBoxMessages.SelectedItem).Name != msg.Name)
            {
                AddLListItem(msg, 0);
            }

        }

        /// <summary>
        /// Tcp / connection changed
        /// </summary>
        /// <param name="_state"></param>
        void TCPConnectionStateUIUpdater(Tcp.TCPConnectionState _state)
        {
            if (_state == Tcp.TCPConnectionState.Connected)
            {
                labelStatus.Text = "Connected";
                labelStatus.BackColor = Color.LightGreen;
                buttonConnect.Text = "Disconnect";
                timer1.Enabled = true;
            }
            else
            {
                timer1.Enabled = false;
                labelStatus.Text = "Disconnected";
                labelStatus.BackColor = Color.LightPink;
                buttonConnect.Text = "Connect";
            }
        }

        private void SendMessage(ComboBox cmb)
        {
            try
            {
                Tcp.Message m = cmb.SelectedItem as Tcp.Message;
                if (Client == null)
                {
                    throw new Exception("Device not connected");
                }
                if (m == null)
                {
                    throw new Exception("No message selected");
                }
                Client.Send(m);
            }
            catch (Exception ex)
            {
                ShowError(null, ex);
            }
        }

        private void SetAutomatedValues(ComboBox cmb)
        {
            try
            {
                Tcp.Message m = cmb.SelectedItem as Tcp.Message;
                if (m != null)
                {
                    foreach (KeyValuePair<String, DataGenerator> dg in DataGenerators)
                    {
                        Tcp.BaseParameter p = m[dg.Key];
                        if (p != null)
                        {
                            p.FromString(dg.Value.GetNextValue());
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                ShowError(null, ex);
            }
        }

        private void buttonSend1_Click(object sender, EventArgs e)
        {
            SendMessage(comboBoxMessages);
        }

        private void buttonSend2_Click(object sender, EventArgs e)
        {
            SendMessage(comboBoxMessages2);
        }

        private void buttonSend3_Click(object sender, EventArgs e)
        {
            SendMessage(comboBoxMessage3);
        }


        private void OnTimerTick(object sender, EventArgs e)
        {
            if (checkBoxTimer1.Checked && DateTime.Now >= Message1ScheduledTimes[0])
            {
                SendMessage(comboBoxMessages);
                Message1ScheduledTimes[0] = DateTime.Now.AddMilliseconds(MessageIntervals[0]);
            }
            if (checkBoxTimer2.Checked && DateTime.Now >= Message1ScheduledTimes[1])
            {
                if (checkBoxAutomate.Checked)
                {
                    SetAutomatedValues(comboBoxMessages2);
                }
                SendMessage(comboBoxMessages2);
                Message1ScheduledTimes[1] = DateTime.Now.AddMilliseconds(MessageIntervals[1]);
            }
            if (checkBox1.Checked && DateTime.Now >= Message1ScheduledTimes[2])
            {
                SendMessage(comboBoxMessage3);
                Message1ScheduledTimes[2] = DateTime.Now.AddMilliseconds(MessageIntervals[2]);
            }
        }


        private void buttonClear_Click(object sender, EventArgs e)
        {
            listViewTransactions.Items.Clear();
            listViewUserMessages.Items.Clear();
            BytesSent = 0;
            BytesReceived = 0;
        }

        void RefreshDataValues(DataGridView dgv)
        {
            foreach (DataGridViewRow r in dgv.Rows)
            {
                RefreshParameterValue(r);
            }
        }

        void RefreshParameterValue(DataGridViewRow row)
        {
            if (row.Cells[1].Tag != null)
            {
                Type t = row.Cells[1].Tag.GetType();
                var v = t.GetProperty("Value");

                row.Cells[1].Value = v.GetValue(row.Cells[1].Tag, null);
                row.Cells[1].ValueType = v.PropertyType;
            }
        }

        void AddDataRows(DataGridView _dgv, IEnumerable<Tcp.BaseParameter> _params)
        {
            foreach (var p in _params)
            {
                DataGridViewRow r = _dgv.Rows[_dgv.Rows.Add()];
                r.Cells[0].Value = p.Name;
                r.Cells[0].ReadOnly = true;
                r.Cells[1].Tag = p;
                RefreshParameterValue(r);
                r.Cells[1].ReadOnly = p.ReadOnly;
            }

        }

        void SetSelectedCommand(ComboBox cb, EventArgs e)
        {
            dataGridViewHeader.Rows.Clear();
            dataGridViewCommand.Rows.Clear();

            Tcp.Message m = cb.SelectedItem as Tcp.Message;
            if (m != null)
            {
                AddDataRows(dataGridViewHeader, m.Header.Params);
                AddDataRows(dataGridViewCommand, m.Params);
            }
        }

        private void OnSelectedCommandChanged(object sender, EventArgs e)
        {
            ComboBox cb = sender as ComboBox;
            SetSelectedCommand(cb, e);
            if (!Initializing)
            {
                if (cb == comboBoxMessages)
                {
                    AppSettings.SelectedMessage1 = cb.SelectedIndex;
                }
                else if (cb == comboBoxMessage3)
                {
                    AppSettings.SelectedMessage3 = cb.SelectedIndex;
                }
                else if (cb == comboBoxMessages2)
                {
                    AppSettings.SelectedMessage2 = cb.SelectedIndex;
                }
                SaveAppSettings(AppSettings, SettingsFilePath);
            }
        }

        private void OnMessageComboBoxEnter(object sender, EventArgs e)
        {
            SetSelectedCommand(sender as ComboBox, e);
        }


        private void OnTimer2Tick(object sender, EventArgs e)
        {
            Tcp.Message m = comboBoxMessages2.SelectedItem as Tcp.Message;
            if (Client != null && m != null)
            {
                Client.Send(m);
            }
        }


        void SetSchedulledTime(CheckBox _cb, int idx)
        {
            if (_cb.Checked)
            {
                Message1ScheduledTimes[idx] = DateTime.Now.AddMilliseconds(MessageIntervals[idx]);
            }
        }

        private void Timer1CheckedChanged(object sender, EventArgs e)
        {
            SetSchedulledTime(sender as CheckBox, 0);
        }

        private void Timer2CheckedChanged(object sender, EventArgs e)
        {
            SetSchedulledTime(sender as CheckBox, 1);
        }

        private void Timer3CheckedChanged(object sender, EventArgs e)
        {
            SetSchedulledTime(sender as CheckBox, 2);
        }

        private void OnNetworkByteOrderChanged(object sender, EventArgs e)
        {
            if (Client != null)
            {
                Client.NetworkByteOrder = checkBoxNetByteOrder.Checked;
            }
            UpdateDeviceProperties();
        }


        private void dataGridView1_CellEndEdit(object sender, DataGridViewCellEventArgs e)
        {
            try
            {
                DataGridView grid = sender as DataGridView;
                DataGridViewCell c = grid.Rows[e.RowIndex].Cells[e.ColumnIndex];
                Tcp.BaseParameter p = c.Tag as Tcp.BaseParameter;
                if (p != null)
                {
                    p.FromString(c.Value.ToString());
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message);
            }
        }

        private void DataGridView_CellBeginEdit(object sender, DataGridViewCellCancelEventArgs e)
        {
            DataGridView grid = sender as DataGridView;
        }

        private void dataGridView_EditingControlShowing(object sender, DataGridViewEditingControlShowingEventArgs e)
        {
            DataGridView grid = sender as DataGridView;
            Tcp.BaseParameter p = grid.CurrentCell.Tag as Tcp.BaseParameter;
            if (p != null && e.Control is DataGridViewTextBoxEditingControl)
            {
                ((DataGridViewTextBoxEditingControl)e.Control).MaxLength = p.DataEntryLength;
            }
        }

        private void comboBoxServerSelectedItemChanged(object sender, EventArgs e)
        {
            Server svr = comboBoxServer.SelectedItem as Server;
            if (svr != null)
            {
                textBoxServer.Text = svr.HostName;
                if (!Initializing)
                {
                    AppSettings.SelectedServer = comboBoxServer.SelectedIndex;
                    SaveAppSettings(AppSettings, SettingsFilePath);
                }
            }
        }

        void SetDeviceInfo(Device _dev, ComboBox cmb)
        {
            foreach (var i in cmb.Items)
            {
                try
                {

                    Tcp.Message m = i as Tcp.Message;
                    if (m != null)
                    {
                        SACMessageHeader hdr = m.Header as SACMessageHeader;
                        if (hdr != null)
                        {
                            hdr.GetField<Tcp.UInt16Parameter>("Equipment Id").Value = (ushort)_dev.DeviceId;
                            dataGridViewHeader.Refresh();
                        }
                    }

                }
                catch (Exception ex)
                {
                    ShowError(null, ex);
                }
            }
        }

        private void comboBoxDeviceSelectedItemChanged(object sender, EventArgs e)
        {
            Device dev = comboBoxDevice.SelectedItem as Device;
            if (dev != null)
            {
                textBoxPort.Text = dev.TcpPort.ToString();
                checkBoxNetByteOrder.Checked = dev.NetworkByteOrder;
                //SetDeviceInfo(dev, comboBoxMessages);
                //SetDeviceInfo(dev, comboBoxMessages2);
                //SetDeviceInfo(dev, comboBoxMessage3);
                RefreshDataValues(dataGridViewHeader);

                textBoxPort.ReadOnly = !dev.Configurable;
                radioButtonClient.Enabled = dev.Configurable;
                radioButtonServer.Enabled = dev.Configurable;
                checkBoxNetByteOrder.Enabled = dev.Configurable; ;

                if (!Initializing)
                {
                    AppSettings.SelectedDevice = comboBoxDevice.SelectedIndex;
                    SaveAppSettings(AppSettings, SettingsFilePath);
                }
            }
        }




        private void listViewTransactionsSizeChanged(object sender, EventArgs e)
        {
            ListView lv = sender as ListView;
            if (lv != null)
            {
                int fixedWidth = 0;
                for (int i = 0; i < lv.Columns.Count - 1; i++)
                {
                    fixedWidth += lv.Columns[i].Width;
                }
                lv.Columns[lv.Columns.Count - 1].Width = lv.Width - fixedWidth;
            }
        }

        private void textBoxTimerKeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                textBoxTimerLeave(sender, e);
            }
        }

        private void textBoxTimerLeave(object sender, EventArgs e)
        {
            try
            {
                TextBox tb = sender as TextBox;
                int val = int.Parse(tb.Text);
                int idx = int.Parse(tb.Tag.ToString());
                MessageIntervals[idx] = val;
                Message1ScheduledTimes[idx] = DateTime.Now.AddMilliseconds(MessageIntervals[idx]);
            }
            catch
            { }
        }

        void UpdateHostName()
        {
            Server svr = comboBoxServer.SelectedItem as Server;
            if (svr != null)
            {
                AppSettings.SetServerHostName(svr.Name, textBoxServer.Text);
            }
        }

        private void textBoxServerLeave(object sender, EventArgs e)
        {
            UpdateHostName();
        }

        private void textBoxServerKeyPress(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                UpdateHostName();
            }
        }

        void UpdateDeviceProperties()
        {
            Device dev = comboBoxDevice.SelectedItem as Device;
            if (dev != null)
            {
                try
                {
                    AppSettings.SetDeviceProperties(dev.Name, int.Parse(textBoxPort.Text), checkBoxNetByteOrder.Checked, radioButtonServer.Checked);
                }
                catch (Exception ex)
                {
                    Logger.Error("Setting device properties", ex);
                    MessageBox.Show(ex.Message);
                }
            }
        }

        private void radioTcpServerCheckedChanged(object sender, EventArgs e)
        {
            UpdateDeviceProperties();
        }

        private void textBoxTcpPortLeave(object sender, EventArgs e)
        {
            UpdateDeviceProperties();
        }

        private void textBoxTcpPortKeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                UpdateDeviceProperties();
            }
        }

        private void ShowStatsClick(object sender, EventArgs e)
        {
            var dlg = new StatsForm();
            dlg.BytesSent = BytesSent;
            dlg.BytesReceived = BytesReceived;
            dlg.ShowDialog();
        }

        private void lblMessage_Click(object sender, EventArgs e)
        {

        }

        private void comboBoxFactory_SelectedIndexChanged(object sender, EventArgs e)
        {
            Factory fct = comboBoxFactory.SelectedItem as Factory;
            if (fct != null && !Initializing)
            {
                ReloadFom(fct);
                RefreshDataValues(dataGridViewHeader);
                AppSettings.SelectedFactory = comboBoxFactory.SelectedIndex;
                SaveAppSettings(AppSettings, SettingsFilePath);
            }
        }      
    }
}
