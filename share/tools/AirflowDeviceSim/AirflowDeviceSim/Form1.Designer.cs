namespace AirflowDeviceSim
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (ClientManager != null)
            {
                ClientManager.StopClients();
            }
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(Form1));
            this.buttonConnect = new System.Windows.Forms.Button();
            this.button1 = new System.Windows.Forms.Button();
            this.textBoxPort = new System.Windows.Forms.TextBox();
            this.labelStatus = new System.Windows.Forms.Label();
            this.timer1 = new System.Windows.Forms.Timer(this.components);
            this.buttonClear = new System.Windows.Forms.Button();
            this.comboBoxMessages = new System.Windows.Forms.ComboBox();
            this.label1 = new System.Windows.Forms.Label();
            this.button2 = new System.Windows.Forms.Button();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.textBoxTimer1 = new System.Windows.Forms.TextBox();
            this.textBoxTimer2 = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.checkBoxTimer1 = new System.Windows.Forms.CheckBox();
            this.checkBoxTimer2 = new System.Windows.Forms.CheckBox();
            this.radioButtonClient = new System.Windows.Forms.RadioButton();
            this.radioButtonServer = new System.Windows.Forms.RadioButton();
            this.checkBoxHide1 = new System.Windows.Forms.CheckBox();
            this.comboBoxMessages2 = new System.Windows.Forms.ComboBox();
            this.checkBoxNetByteOrder = new System.Windows.Forms.CheckBox();
            this.label6 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.dataGridViewHeader = new System.Windows.Forms.DataGridView();
            this.Column1 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.Column2 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.dataGridViewCommand = new System.Windows.Forms.DataGridView();
            this.dataGridViewTextBoxColumn1 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.dataGridViewTextBoxColumn2 = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.comboBoxServer = new System.Windows.Forms.ComboBox();
            this.splitContainerVertical = new System.Windows.Forms.SplitContainer();
            this.splitContainer3 = new System.Windows.Forms.SplitContainer();
            this.listViewTransactions = new System.Windows.Forms.ListView();
            this.columnHeader1 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.columnHeader2 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.columnHeader3 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.imageList1 = new System.Windows.Forms.ImageList(this.components);
            this.listViewUserMessages = new System.Windows.Forms.ListView();
            this.columnHeader4 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.columnHeader5 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.columnHeader6 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.splitContainerHorzRight = new System.Windows.Forms.SplitContainer();
            this.label8 = new System.Windows.Forms.Label();
            this.textBoxServer = new System.Windows.Forms.TextBox();
            this.label9 = new System.Windows.Forms.Label();
            this.comboBoxDevice = new System.Windows.Forms.ComboBox();
            this.comboBoxMessage3 = new System.Windows.Forms.ComboBox();
            this.label10 = new System.Windows.Forms.Label();
            this.checkBox1 = new System.Windows.Forms.CheckBox();
            this.label11 = new System.Windows.Forms.Label();
            this.textBoxTimer3 = new System.Windows.Forms.TextBox();
            this.button3 = new System.Windows.Forms.Button();
            this.label12 = new System.Windows.Forms.Label();
            this.checkBoxAutomate = new System.Windows.Forms.CheckBox();
            this.buttonStats = new System.Windows.Forms.Button();
            this.lblMessage = new System.Windows.Forms.Label();
            this.comboBoxFactory = new System.Windows.Forms.ComboBox();
            ((System.ComponentModel.ISupportInitialize)(this.dataGridViewHeader)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.dataGridViewCommand)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainerVertical)).BeginInit();
            this.splitContainerVertical.Panel1.SuspendLayout();
            this.splitContainerVertical.Panel2.SuspendLayout();
            this.splitContainerVertical.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer3)).BeginInit();
            this.splitContainer3.Panel1.SuspendLayout();
            this.splitContainer3.Panel2.SuspendLayout();
            this.splitContainer3.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainerHorzRight)).BeginInit();
            this.splitContainerHorzRight.Panel1.SuspendLayout();
            this.splitContainerHorzRight.Panel2.SuspendLayout();
            this.splitContainerHorzRight.SuspendLayout();
            this.SuspendLayout();
            // 
            // buttonConnect
            // 
            this.buttonConnect.Location = new System.Drawing.Point(18, 85);
            this.buttonConnect.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.buttonConnect.Name = "buttonConnect";
            this.buttonConnect.Size = new System.Drawing.Size(116, 38);
            this.buttonConnect.TabIndex = 0;
            this.buttonConnect.Text = "Connect";
            this.buttonConnect.UseVisualStyleBackColor = true;
            this.buttonConnect.Click += new System.EventHandler(this.buttonConnect_Click);
            // 
            // button1
            // 
            this.button1.Location = new System.Drawing.Point(1181, 5);
            this.button1.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.button1.Name = "button1";
            this.button1.Size = new System.Drawing.Size(87, 35);
            this.button1.TabIndex = 1;
            this.button1.Text = "Send";
            this.button1.UseVisualStyleBackColor = true;
            this.button1.Click += new System.EventHandler(this.buttonSend1_Click);
            // 
            // textBoxPort
            // 
            this.textBoxPort.Location = new System.Drawing.Point(494, 49);
            this.textBoxPort.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.textBoxPort.Name = "textBoxPort";
            this.textBoxPort.Size = new System.Drawing.Size(98, 26);
            this.textBoxPort.TabIndex = 3;
            this.textBoxPort.Text = "12001";
            this.textBoxPort.KeyDown += new System.Windows.Forms.KeyEventHandler(this.textBoxTcpPortKeyDown);
            this.textBoxPort.Leave += new System.EventHandler(this.textBoxTcpPortLeave);
            // 
            // labelStatus
            // 
            this.labelStatus.AutoSize = true;
            this.labelStatus.BorderStyle = System.Windows.Forms.BorderStyle.Fixed3D;
            this.labelStatus.Location = new System.Drawing.Point(152, 92);
            this.labelStatus.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.labelStatus.Name = "labelStatus";
            this.labelStatus.Size = new System.Drawing.Size(109, 22);
            this.labelStatus.TabIndex = 6;
            this.labelStatus.Text = "Disconnected";
            // 
            // timer1
            // 
            this.timer1.Tick += new System.EventHandler(this.OnTimerTick);
            // 
            // buttonClear
            // 
            this.buttonClear.Location = new System.Drawing.Point(722, 88);
            this.buttonClear.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.buttonClear.Name = "buttonClear";
            this.buttonClear.Size = new System.Drawing.Size(89, 35);
            this.buttonClear.TabIndex = 9;
            this.buttonClear.Text = "Clear Logs";
            this.buttonClear.UseVisualStyleBackColor = true;
            this.buttonClear.Click += new System.EventHandler(this.buttonClear_Click);
            // 
            // comboBoxMessages
            // 
            this.comboBoxMessages.DisplayMember = "Header.Name";
            this.comboBoxMessages.FormattingEnabled = true;
            this.comboBoxMessages.Location = new System.Drawing.Point(918, 8);
            this.comboBoxMessages.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxMessages.Name = "comboBoxMessages";
            this.comboBoxMessages.Size = new System.Drawing.Size(257, 28);
            this.comboBoxMessages.TabIndex = 11;
            this.comboBoxMessages.SelectedIndexChanged += new System.EventHandler(this.OnSelectedCommandChanged);
            this.comboBoxMessages.Enter += new System.EventHandler(this.OnMessageComboBoxEnter);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(414, 58);
            this.label1.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(68, 20);
            this.label1.TabIndex = 15;
            this.label1.Text = "Tcp Port";
            // 
            // button2
            // 
            this.button2.Location = new System.Drawing.Point(1181, 45);
            this.button2.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.button2.Name = "button2";
            this.button2.Size = new System.Drawing.Size(87, 35);
            this.button2.TabIndex = 1;
            this.button2.Text = "Send";
            this.button2.UseVisualStyleBackColor = true;
            this.button2.Click += new System.EventHandler(this.buttonSend2_Click);
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(820, 16);
            this.label2.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(87, 20);
            this.label2.TabIndex = 17;
            this.label2.Text = "Message 1";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(820, 59);
            this.label3.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(87, 20);
            this.label3.TabIndex = 17;
            this.label3.Text = "Message 2";
            // 
            // textBoxTimer1
            // 
            this.textBoxTimer1.Location = new System.Drawing.Point(1307, 11);
            this.textBoxTimer1.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.textBoxTimer1.Name = "textBoxTimer1";
            this.textBoxTimer1.Size = new System.Drawing.Size(79, 26);
            this.textBoxTimer1.TabIndex = 18;
            this.textBoxTimer1.Tag = "0";
            this.textBoxTimer1.Text = "10000";
            this.textBoxTimer1.KeyDown += new System.Windows.Forms.KeyEventHandler(this.textBoxTimerKeyDown);
            this.textBoxTimer1.Leave += new System.EventHandler(this.textBoxTimerLeave);
            // 
            // textBoxTimer2
            // 
            this.textBoxTimer2.Location = new System.Drawing.Point(1307, 52);
            this.textBoxTimer2.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.textBoxTimer2.Name = "textBoxTimer2";
            this.textBoxTimer2.Size = new System.Drawing.Size(79, 26);
            this.textBoxTimer2.TabIndex = 18;
            this.textBoxTimer2.Tag = "1";
            this.textBoxTimer2.Text = "10000";
            this.textBoxTimer2.KeyDown += new System.Windows.Forms.KeyEventHandler(this.textBoxTimerKeyDown);
            this.textBoxTimer2.Leave += new System.EventHandler(this.textBoxTimerLeave);
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(1396, 18);
            this.label4.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(30, 20);
            this.label4.TabIndex = 19;
            this.label4.Text = "ms";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(1395, 54);
            this.label5.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(30, 20);
            this.label5.TabIndex = 19;
            this.label5.Text = "ms";
            // 
            // checkBoxTimer1
            // 
            this.checkBoxTimer1.AutoSize = true;
            this.checkBoxTimer1.Location = new System.Drawing.Point(1278, 16);
            this.checkBoxTimer1.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBoxTimer1.Name = "checkBoxTimer1";
            this.checkBoxTimer1.Size = new System.Drawing.Size(22, 21);
            this.checkBoxTimer1.TabIndex = 20;
            this.checkBoxTimer1.UseVisualStyleBackColor = true;
            this.checkBoxTimer1.CheckedChanged += new System.EventHandler(this.Timer1CheckedChanged);
            // 
            // checkBoxTimer2
            // 
            this.checkBoxTimer2.AutoSize = true;
            this.checkBoxTimer2.Location = new System.Drawing.Point(1277, 59);
            this.checkBoxTimer2.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBoxTimer2.Name = "checkBoxTimer2";
            this.checkBoxTimer2.Size = new System.Drawing.Size(22, 21);
            this.checkBoxTimer2.TabIndex = 20;
            this.checkBoxTimer2.UseVisualStyleBackColor = true;
            this.checkBoxTimer2.CheckedChanged += new System.EventHandler(this.Timer2CheckedChanged);
            // 
            // radioButtonClient
            // 
            this.radioButtonClient.AutoSize = true;
            this.radioButtonClient.Checked = true;
            this.radioButtonClient.Location = new System.Drawing.Point(288, 58);
            this.radioButtonClient.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.radioButtonClient.Name = "radioButtonClient";
            this.radioButtonClient.Size = new System.Drawing.Size(108, 24);
            this.radioButtonClient.TabIndex = 21;
            this.radioButtonClient.TabStop = true;
            this.radioButtonClient.Text = "TCP Client";
            this.radioButtonClient.UseVisualStyleBackColor = true;
            // 
            // radioButtonServer
            // 
            this.radioButtonServer.AutoSize = true;
            this.radioButtonServer.Location = new System.Drawing.Point(287, 86);
            this.radioButtonServer.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.radioButtonServer.Name = "radioButtonServer";
            this.radioButtonServer.Size = new System.Drawing.Size(114, 24);
            this.radioButtonServer.TabIndex = 22;
            this.radioButtonServer.Text = "TCP Server";
            this.radioButtonServer.UseVisualStyleBackColor = true;
            this.radioButtonServer.CheckedChanged += new System.EventHandler(this.radioTcpServerCheckedChanged);
            // 
            // checkBoxHide1
            // 
            this.checkBoxHide1.AutoSize = true;
            this.checkBoxHide1.Location = new System.Drawing.Point(1441, 42);
            this.checkBoxHide1.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBoxHide1.Name = "checkBoxHide1";
            this.checkBoxHide1.Size = new System.Drawing.Size(68, 24);
            this.checkBoxHide1.TabIndex = 23;
            this.checkBoxHide1.Text = "Hide";
            this.checkBoxHide1.UseVisualStyleBackColor = true;
            // 
            // comboBoxMessages2
            // 
            this.comboBoxMessages2.FormattingEnabled = true;
            this.comboBoxMessages2.Location = new System.Drawing.Point(918, 48);
            this.comboBoxMessages2.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxMessages2.Name = "comboBoxMessages2";
            this.comboBoxMessages2.Size = new System.Drawing.Size(257, 28);
            this.comboBoxMessages2.TabIndex = 24;
            this.comboBoxMessages2.SelectedIndexChanged += new System.EventHandler(this.OnSelectedCommandChanged);
            this.comboBoxMessages2.Enter += new System.EventHandler(this.OnMessageComboBoxEnter);
            // 
            // checkBoxNetByteOrder
            // 
            this.checkBoxNetByteOrder.AutoSize = true;
            this.checkBoxNetByteOrder.Location = new System.Drawing.Point(415, 89);
            this.checkBoxNetByteOrder.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBoxNetByteOrder.Name = "checkBoxNetByteOrder";
            this.checkBoxNetByteOrder.Size = new System.Drawing.Size(173, 24);
            this.checkBoxNetByteOrder.TabIndex = 25;
            this.checkBoxNetByteOrder.Text = "Network Byte Order";
            this.checkBoxNetByteOrder.UseVisualStyleBackColor = true;
            this.checkBoxNetByteOrder.CheckedChanged += new System.EventHandler(this.OnNetworkByteOrderChanged);
            // 
            // label6
            // 
            this.label6.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label6.AutoSize = true;
            this.label6.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label6.Location = new System.Drawing.Point(-90, 0);
            this.label6.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(165, 20);
            this.label6.TabIndex = 26;
            this.label6.Text = "Command Header ";
            // 
            // label7
            // 
            this.label7.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label7.AutoSize = true;
            this.label7.Font = new System.Drawing.Font("Microsoft Sans Serif", 8.25F, System.Drawing.FontStyle.Bold, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.label7.Location = new System.Drawing.Point(-67, 20);
            this.label7.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(195, 20);
            this.label7.TabIndex = 27;
            this.label7.Text = "Command Parameters";
            // 
            // dataGridViewHeader
            // 
            this.dataGridViewHeader.AllowUserToAddRows = false;
            this.dataGridViewHeader.AllowUserToDeleteRows = false;
            this.dataGridViewHeader.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dataGridViewHeader.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.Column1,
            this.Column2});
            this.dataGridViewHeader.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dataGridViewHeader.Location = new System.Drawing.Point(0, 0);
            this.dataGridViewHeader.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.dataGridViewHeader.Name = "dataGridViewHeader";
            this.dataGridViewHeader.RowHeadersVisible = false;
            this.dataGridViewHeader.Size = new System.Drawing.Size(484, 243);
            this.dataGridViewHeader.TabIndex = 28;
            this.dataGridViewHeader.CellEndEdit += new System.Windows.Forms.DataGridViewCellEventHandler(this.dataGridView1_CellEndEdit);
            this.dataGridViewHeader.EditingControlShowing += new System.Windows.Forms.DataGridViewEditingControlShowingEventHandler(this.dataGridView_EditingControlShowing);
            // 
            // Column1
            // 
            this.Column1.HeaderText = "Header";
            this.Column1.Name = "Column1";
            // 
            // Column2
            // 
            this.Column2.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.Fill;
            this.Column2.HeaderText = "Value";
            this.Column2.Name = "Column2";
            // 
            // dataGridViewCommand
            // 
            this.dataGridViewCommand.AllowUserToAddRows = false;
            this.dataGridViewCommand.AllowUserToDeleteRows = false;
            this.dataGridViewCommand.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.dataGridViewCommand.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.dataGridViewTextBoxColumn1,
            this.dataGridViewTextBoxColumn2});
            this.dataGridViewCommand.Dock = System.Windows.Forms.DockStyle.Fill;
            this.dataGridViewCommand.Location = new System.Drawing.Point(0, 0);
            this.dataGridViewCommand.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.dataGridViewCommand.Name = "dataGridViewCommand";
            this.dataGridViewCommand.RowHeadersVisible = false;
            this.dataGridViewCommand.Size = new System.Drawing.Size(484, 555);
            this.dataGridViewCommand.TabIndex = 29;
            this.dataGridViewCommand.CellBeginEdit += new System.Windows.Forms.DataGridViewCellCancelEventHandler(this.DataGridView_CellBeginEdit);
            this.dataGridViewCommand.CellEndEdit += new System.Windows.Forms.DataGridViewCellEventHandler(this.dataGridView1_CellEndEdit);
            this.dataGridViewCommand.EditingControlShowing += new System.Windows.Forms.DataGridViewEditingControlShowingEventHandler(this.dataGridView_EditingControlShowing);
            // 
            // dataGridViewTextBoxColumn1
            // 
            this.dataGridViewTextBoxColumn1.HeaderText = "Parameter";
            this.dataGridViewTextBoxColumn1.Name = "dataGridViewTextBoxColumn1";
            // 
            // dataGridViewTextBoxColumn2
            // 
            this.dataGridViewTextBoxColumn2.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.Fill;
            this.dataGridViewTextBoxColumn2.HeaderText = "Value";
            this.dataGridViewTextBoxColumn2.Name = "dataGridViewTextBoxColumn2";
            // 
            // comboBoxServer
            // 
            this.comboBoxServer.DisplayMember = "Name";
            this.comboBoxServer.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxServer.FormattingEnabled = true;
            this.comboBoxServer.Location = new System.Drawing.Point(94, 11);
            this.comboBoxServer.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxServer.Name = "comboBoxServer";
            this.comboBoxServer.Size = new System.Drawing.Size(149, 28);
            this.comboBoxServer.TabIndex = 30;
            this.comboBoxServer.SelectedIndexChanged += new System.EventHandler(this.comboBoxServerSelectedItemChanged);
            // 
            // splitContainerVertical
            // 
            this.splitContainerVertical.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.splitContainerVertical.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.splitContainerVertical.Location = new System.Drawing.Point(3, 126);
            this.splitContainerVertical.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.splitContainerVertical.Name = "splitContainerVertical";
            // 
            // splitContainerVertical.Panel1
            // 
            this.splitContainerVertical.Panel1.Controls.Add(this.splitContainer3);
            // 
            // splitContainerVertical.Panel2
            // 
            this.splitContainerVertical.Panel2.Controls.Add(this.splitContainerHorzRight);
            this.splitContainerVertical.Size = new System.Drawing.Size(1659, 808);
            this.splitContainerVertical.SplitterDistance = 1167;
            this.splitContainerVertical.SplitterWidth = 6;
            this.splitContainerVertical.TabIndex = 31;
            // 
            // splitContainer3
            // 
            this.splitContainer3.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.splitContainer3.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainer3.Location = new System.Drawing.Point(0, 0);
            this.splitContainer3.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.splitContainer3.Name = "splitContainer3";
            this.splitContainer3.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainer3.Panel1
            // 
            this.splitContainer3.Panel1.Controls.Add(this.listViewTransactions);
            // 
            // splitContainer3.Panel2
            // 
            this.splitContainer3.Panel2.Controls.Add(this.listViewUserMessages);
            this.splitContainer3.Size = new System.Drawing.Size(1167, 808);
            this.splitContainer3.SplitterDistance = 545;
            this.splitContainer3.SplitterWidth = 6;
            this.splitContainer3.TabIndex = 0;
            // 
            // listViewTransactions
            // 
            this.listViewTransactions.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.columnHeader1,
            this.columnHeader2,
            this.columnHeader3});
            this.listViewTransactions.Dock = System.Windows.Forms.DockStyle.Fill;
            this.listViewTransactions.Location = new System.Drawing.Point(0, 0);
            this.listViewTransactions.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.listViewTransactions.Name = "listViewTransactions";
            this.listViewTransactions.Size = new System.Drawing.Size(1165, 543);
            this.listViewTransactions.SmallImageList = this.imageList1;
            this.listViewTransactions.TabIndex = 0;
            this.listViewTransactions.UseCompatibleStateImageBehavior = false;
            this.listViewTransactions.View = System.Windows.Forms.View.Details;
            this.listViewTransactions.SizeChanged += new System.EventHandler(this.listViewTransactionsSizeChanged);
            // 
            // columnHeader1
            // 
            this.columnHeader1.Text = "";
            this.columnHeader1.Width = 20;
            // 
            // columnHeader2
            // 
            this.columnHeader2.Text = "Time";
            this.columnHeader2.Width = 140;
            // 
            // columnHeader3
            // 
            this.columnHeader3.Text = "Message Content";
            this.columnHeader3.Width = 558;
            // 
            // imageList1
            // 
            this.imageList1.ImageStream = ((System.Windows.Forms.ImageListStreamer)(resources.GetObject("imageList1.ImageStream")));
            this.imageList1.TransparentColor = System.Drawing.Color.Transparent;
            this.imageList1.Images.SetKeyName(0, "LeftArrow.jpg");
            this.imageList1.Images.SetKeyName(1, "RightArrow.jpg");
            this.imageList1.Images.SetKeyName(2, "Red-Cross.png");
            // 
            // listViewUserMessages
            // 
            this.listViewUserMessages.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.columnHeader4,
            this.columnHeader5,
            this.columnHeader6});
            this.listViewUserMessages.Dock = System.Windows.Forms.DockStyle.Fill;
            this.listViewUserMessages.Location = new System.Drawing.Point(0, 0);
            this.listViewUserMessages.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.listViewUserMessages.Name = "listViewUserMessages";
            this.listViewUserMessages.Size = new System.Drawing.Size(1165, 255);
            this.listViewUserMessages.SmallImageList = this.imageList1;
            this.listViewUserMessages.TabIndex = 0;
            this.listViewUserMessages.UseCompatibleStateImageBehavior = false;
            this.listViewUserMessages.View = System.Windows.Forms.View.Details;
            this.listViewUserMessages.SizeChanged += new System.EventHandler(this.listViewTransactionsSizeChanged);
            // 
            // columnHeader4
            // 
            this.columnHeader4.Text = "";
            this.columnHeader4.Width = 20;
            // 
            // columnHeader5
            // 
            this.columnHeader5.Text = "Time";
            this.columnHeader5.Width = 90;
            // 
            // columnHeader6
            // 
            this.columnHeader6.Text = "Message";
            this.columnHeader6.Width = 608;
            // 
            // splitContainerHorzRight
            // 
            this.splitContainerHorzRight.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.splitContainerHorzRight.Dock = System.Windows.Forms.DockStyle.Fill;
            this.splitContainerHorzRight.Location = new System.Drawing.Point(0, 0);
            this.splitContainerHorzRight.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.splitContainerHorzRight.Name = "splitContainerHorzRight";
            this.splitContainerHorzRight.Orientation = System.Windows.Forms.Orientation.Horizontal;
            // 
            // splitContainerHorzRight.Panel1
            // 
            this.splitContainerHorzRight.Panel1.Controls.Add(this.dataGridViewHeader);
            this.splitContainerHorzRight.Panel1.Controls.Add(this.label6);
            // 
            // splitContainerHorzRight.Panel2
            // 
            this.splitContainerHorzRight.Panel2.Controls.Add(this.dataGridViewCommand);
            this.splitContainerHorzRight.Panel2.Controls.Add(this.label7);
            this.splitContainerHorzRight.Size = new System.Drawing.Size(486, 808);
            this.splitContainerHorzRight.SplitterDistance = 245;
            this.splitContainerHorzRight.SplitterWidth = 6;
            this.splitContainerHorzRight.TabIndex = 0;
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(29, 15);
            this.label8.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(55, 20);
            this.label8.TabIndex = 32;
            this.label8.Text = "Server";
            // 
            // textBoxServer
            // 
            this.textBoxServer.Location = new System.Drawing.Point(33, 48);
            this.textBoxServer.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.textBoxServer.Name = "textBoxServer";
            this.textBoxServer.Size = new System.Drawing.Size(211, 26);
            this.textBoxServer.TabIndex = 33;
            this.textBoxServer.KeyDown += new System.Windows.Forms.KeyEventHandler(this.textBoxServerKeyPress);
            this.textBoxServer.Leave += new System.EventHandler(this.textBoxServerLeave);
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.Location = new System.Drawing.Point(253, 15);
            this.label9.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(57, 20);
            this.label9.TabIndex = 34;
            this.label9.Text = "Device";
            // 
            // comboBoxDevice
            // 
            this.comboBoxDevice.DisplayMember = "Name";
            this.comboBoxDevice.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxDevice.FormattingEnabled = true;
            this.comboBoxDevice.Location = new System.Drawing.Point(320, 9);
            this.comboBoxDevice.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxDevice.Name = "comboBoxDevice";
            this.comboBoxDevice.Size = new System.Drawing.Size(154, 28);
            this.comboBoxDevice.TabIndex = 35;
            this.comboBoxDevice.SelectedIndexChanged += new System.EventHandler(this.comboBoxDeviceSelectedItemChanged);
            // 
            // comboBoxMessage3
            // 
            this.comboBoxMessage3.FormattingEnabled = true;
            this.comboBoxMessage3.Location = new System.Drawing.Point(918, 91);
            this.comboBoxMessage3.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxMessage3.Name = "comboBoxMessage3";
            this.comboBoxMessage3.Size = new System.Drawing.Size(257, 28);
            this.comboBoxMessage3.TabIndex = 37;
            this.comboBoxMessage3.SelectedIndexChanged += new System.EventHandler(this.OnSelectedCommandChanged);
            this.comboBoxMessage3.Enter += new System.EventHandler(this.OnMessageComboBoxEnter);
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(820, 102);
            this.label10.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(87, 20);
            this.label10.TabIndex = 36;
            this.label10.Text = "Message 3";
            // 
            // checkBox1
            // 
            this.checkBox1.AutoSize = true;
            this.checkBox1.Location = new System.Drawing.Point(1277, 99);
            this.checkBox1.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBox1.Name = "checkBox1";
            this.checkBox1.Size = new System.Drawing.Size(22, 21);
            this.checkBox1.TabIndex = 41;
            this.checkBox1.UseVisualStyleBackColor = true;
            this.checkBox1.CheckedChanged += new System.EventHandler(this.Timer3CheckedChanged);
            // 
            // label11
            // 
            this.label11.AutoSize = true;
            this.label11.Location = new System.Drawing.Point(1397, 95);
            this.label11.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label11.Name = "label11";
            this.label11.Size = new System.Drawing.Size(30, 20);
            this.label11.TabIndex = 40;
            this.label11.Text = "ms";
            // 
            // textBoxTimer3
            // 
            this.textBoxTimer3.Location = new System.Drawing.Point(1307, 94);
            this.textBoxTimer3.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.textBoxTimer3.Name = "textBoxTimer3";
            this.textBoxTimer3.Size = new System.Drawing.Size(79, 26);
            this.textBoxTimer3.TabIndex = 39;
            this.textBoxTimer3.Tag = "2";
            this.textBoxTimer3.Text = "10000";
            this.textBoxTimer3.KeyDown += new System.Windows.Forms.KeyEventHandler(this.textBoxTimerKeyDown);
            this.textBoxTimer3.Leave += new System.EventHandler(this.textBoxTimerLeave);
            // 
            // button3
            // 
            this.button3.Location = new System.Drawing.Point(1181, 89);
            this.button3.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.button3.Name = "button3";
            this.button3.Size = new System.Drawing.Size(87, 35);
            this.button3.TabIndex = 38;
            this.button3.Text = "Send";
            this.button3.UseVisualStyleBackColor = true;
            this.button3.Click += new System.EventHandler(this.buttonSend3_Click);
            // 
            // label12
            // 
            this.label12.AutoSize = true;
            this.label12.Location = new System.Drawing.Point(1438, 74);
            this.label12.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.label12.Name = "label12";
            this.label12.Size = new System.Drawing.Size(77, 20);
            this.label12.TabIndex = 42;
            this.label12.Text = "Keepalive";
            // 
            // checkBoxAutomate
            // 
            this.checkBoxAutomate.AutoSize = true;
            this.checkBoxAutomate.Location = new System.Drawing.Point(615, 54);
            this.checkBoxAutomate.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.checkBoxAutomate.Name = "checkBoxAutomate";
            this.checkBoxAutomate.Size = new System.Drawing.Size(105, 24);
            this.checkBoxAutomate.TabIndex = 43;
            this.checkBoxAutomate.Text = "Automate";
            this.checkBoxAutomate.UseVisualStyleBackColor = true;
            // 
            // buttonStats
            // 
            this.buttonStats.Location = new System.Drawing.Point(724, 48);
            this.buttonStats.Margin = new System.Windows.Forms.Padding(3, 4, 3, 4);
            this.buttonStats.Name = "buttonStats";
            this.buttonStats.Size = new System.Drawing.Size(87, 35);
            this.buttonStats.TabIndex = 44;
            this.buttonStats.Text = "Statistics";
            this.buttonStats.UseVisualStyleBackColor = true;
            this.buttonStats.Click += new System.EventHandler(this.ShowStatsClick);
            // 
            // lblMessage
            // 
            this.lblMessage.AutoSize = true;
            this.lblMessage.Location = new System.Drawing.Point(487, 15);
            this.lblMessage.Margin = new System.Windows.Forms.Padding(4, 0, 4, 0);
            this.lblMessage.Name = "lblMessage";
            this.lblMessage.Size = new System.Drawing.Size(77, 20);
            this.lblMessage.TabIndex = 45;
            this.lblMessage.Text = "Msg Type";
            this.lblMessage.Click += new System.EventHandler(this.lblMessage_Click);
            // 
            // comboBoxFactory
            // 
            this.comboBoxFactory.DisplayMember = "Name";
            this.comboBoxFactory.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.comboBoxFactory.FormattingEnabled = true;
            this.comboBoxFactory.Location = new System.Drawing.Point(575, 14);
            this.comboBoxFactory.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.comboBoxFactory.Name = "comboBoxFactory";
            this.comboBoxFactory.Size = new System.Drawing.Size(236, 28);
            this.comboBoxFactory.TabIndex = 46;
            this.comboBoxFactory.SelectedIndexChanged += new System.EventHandler(this.comboBoxFactory_SelectedIndexChanged);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(9F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1663, 932);
            this.Controls.Add(this.comboBoxFactory);
            this.Controls.Add(this.lblMessage);
            this.Controls.Add(this.buttonStats);
            this.Controls.Add(this.checkBoxAutomate);
            this.Controls.Add(this.label12);
            this.Controls.Add(this.checkBox1);
            this.Controls.Add(this.label11);
            this.Controls.Add(this.textBoxTimer3);
            this.Controls.Add(this.button3);
            this.Controls.Add(this.comboBoxMessage3);
            this.Controls.Add(this.label10);
            this.Controls.Add(this.comboBoxDevice);
            this.Controls.Add(this.label9);
            this.Controls.Add(this.textBoxServer);
            this.Controls.Add(this.label8);
            this.Controls.Add(this.splitContainerVertical);
            this.Controls.Add(this.comboBoxServer);
            this.Controls.Add(this.checkBoxNetByteOrder);
            this.Controls.Add(this.comboBoxMessages2);
            this.Controls.Add(this.checkBoxHide1);
            this.Controls.Add(this.radioButtonServer);
            this.Controls.Add(this.radioButtonClient);
            this.Controls.Add(this.checkBoxTimer2);
            this.Controls.Add(this.checkBoxTimer1);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.textBoxTimer2);
            this.Controls.Add(this.textBoxTimer1);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.comboBoxMessages);
            this.Controls.Add(this.buttonClear);
            this.Controls.Add(this.labelStatus);
            this.Controls.Add(this.textBoxPort);
            this.Controls.Add(this.button2);
            this.Controls.Add(this.button1);
            this.Controls.Add(this.buttonConnect);
            this.Margin = new System.Windows.Forms.Padding(4, 5, 4, 5);
            this.Name = "Form1";
            this.Text = " Airflow Device Simulator";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.OnFormClosing);
            this.Load += new System.EventHandler(this.OnFormLoad);
            ((System.ComponentModel.ISupportInitialize)(this.dataGridViewHeader)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.dataGridViewCommand)).EndInit();
            this.splitContainerVertical.Panel1.ResumeLayout(false);
            this.splitContainerVertical.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainerVertical)).EndInit();
            this.splitContainerVertical.ResumeLayout(false);
            this.splitContainer3.Panel1.ResumeLayout(false);
            this.splitContainer3.Panel2.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.splitContainer3)).EndInit();
            this.splitContainer3.ResumeLayout(false);
            this.splitContainerHorzRight.Panel1.ResumeLayout(false);
            this.splitContainerHorzRight.Panel1.PerformLayout();
            this.splitContainerHorzRight.Panel2.ResumeLayout(false);
            this.splitContainerHorzRight.Panel2.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.splitContainerHorzRight)).EndInit();
            this.splitContainerHorzRight.ResumeLayout(false);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button buttonConnect;
        private System.Windows.Forms.Button button1;
        private System.Windows.Forms.TextBox textBoxPort;
        private System.Windows.Forms.Label labelStatus;
        private System.Windows.Forms.Timer timer1;
        private System.Windows.Forms.Button buttonClear;
        private System.Windows.Forms.ComboBox comboBoxMessages;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Button button2;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox textBoxTimer1;
        private System.Windows.Forms.TextBox textBoxTimer2;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.CheckBox checkBoxTimer1;
        private System.Windows.Forms.CheckBox checkBoxTimer2;
        private System.Windows.Forms.RadioButton radioButtonClient;
        private System.Windows.Forms.RadioButton radioButtonServer;
        private System.Windows.Forms.CheckBox checkBoxHide1;
        private System.Windows.Forms.ComboBox comboBoxMessages2;
        private System.Windows.Forms.CheckBox checkBoxNetByteOrder;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.DataGridView dataGridViewHeader;
        private System.Windows.Forms.DataGridView dataGridViewCommand;
        private System.Windows.Forms.DataGridViewTextBoxColumn dataGridViewTextBoxColumn1;
        private System.Windows.Forms.DataGridViewTextBoxColumn dataGridViewTextBoxColumn2;
        private System.Windows.Forms.ComboBox comboBoxServer;
        private System.Windows.Forms.SplitContainer splitContainerVertical;
        private System.Windows.Forms.SplitContainer splitContainer3;
        private System.Windows.Forms.SplitContainer splitContainerHorzRight;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.TextBox textBoxServer;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.ComboBox comboBoxDevice;
        private System.Windows.Forms.ListView listViewTransactions;
        private System.Windows.Forms.ColumnHeader columnHeader1;
        private System.Windows.Forms.ColumnHeader columnHeader2;
        private System.Windows.Forms.ImageList imageList1;
        private System.Windows.Forms.ColumnHeader columnHeader3;
        private System.Windows.Forms.ComboBox comboBoxMessage3;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.CheckBox checkBox1;
        private System.Windows.Forms.Label label11;
        private System.Windows.Forms.TextBox textBoxTimer3;
        private System.Windows.Forms.Button button3;
        private System.Windows.Forms.ListView listViewUserMessages;
        private System.Windows.Forms.ColumnHeader columnHeader4;
        private System.Windows.Forms.ColumnHeader columnHeader5;
        private System.Windows.Forms.ColumnHeader columnHeader6;
        private System.Windows.Forms.Label label12;
        private System.Windows.Forms.DataGridViewTextBoxColumn Column1;
        private System.Windows.Forms.DataGridViewTextBoxColumn Column2;
        private System.Windows.Forms.CheckBox checkBoxAutomate;
        private System.Windows.Forms.Button buttonStats;
        private System.Windows.Forms.Label lblMessage;
        private System.Windows.Forms.ComboBox comboBoxFactory;
    }
}

