package com.daifukuamerica.wrxj.swingui.utility;

import com.daifukuamerica.wrxj.application.Application;
import com.daifukuamerica.wrxj.dataserver.standard.StandardInventoryServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLoadServer;
import com.daifukuamerica.wrxj.dataserver.standard.StandardLocationServer;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerType;
import com.daifukuamerica.wrxj.dbadapter.data.ContainerTypeData;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfig;
import com.daifukuamerica.wrxj.dbadapter.data.ControllerConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.Customer;
import com.daifukuamerica.wrxj.dbadapter.data.CustomerData;
import com.daifukuamerica.wrxj.dbadapter.data.Device;
import com.daifukuamerica.wrxj.dbadapter.data.DeviceData;
import com.daifukuamerica.wrxj.dbadapter.data.Employee;
import com.daifukuamerica.wrxj.dbadapter.data.EmployeeData;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfig;
import com.daifukuamerica.wrxj.dbadapter.data.HostConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccess;
import com.daifukuamerica.wrxj.dbadapter.data.HostOutAccessData;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMaster;
import com.daifukuamerica.wrxj.dbadapter.data.ItemMasterData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadData;
import com.daifukuamerica.wrxj.dbadapter.data.LoadLineItemData;
import com.daifukuamerica.wrxj.dbadapter.data.LocationData;
import com.daifukuamerica.wrxj.dbadapter.data.Login;
import com.daifukuamerica.wrxj.dbadapter.data.LoginData;
import com.daifukuamerica.wrxj.dbadapter.data.Move;
import com.daifukuamerica.wrxj.dbadapter.data.MoveData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeader;
import com.daifukuamerica.wrxj.dbadapter.data.OrderHeaderData;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLine;
import com.daifukuamerica.wrxj.dbadapter.data.OrderLineData;
import com.daifukuamerica.wrxj.dbadapter.data.Port;
import com.daifukuamerica.wrxj.dbadapter.data.PortData;
import com.daifukuamerica.wrxj.dbadapter.data.Role;
import com.daifukuamerica.wrxj.dbadapter.data.RoleData;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOption;
import com.daifukuamerica.wrxj.dbadapter.data.RoleOptionData;
import com.daifukuamerica.wrxj.dbadapter.data.Route;
import com.daifukuamerica.wrxj.dbadapter.data.RouteData;
import com.daifukuamerica.wrxj.dbadapter.data.Station;
import com.daifukuamerica.wrxj.dbadapter.data.StationData;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfig;
import com.daifukuamerica.wrxj.dbadapter.data.SysConfigData;
import com.daifukuamerica.wrxj.dbadapter.data.Warehouse;
import com.daifukuamerica.wrxj.dbadapter.data.WarehouseData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBMetaData;
import com.daifukuamerica.wrxj.swing.SKDCButton;
import com.daifukuamerica.wrxj.swing.SKDCInternalFrame;
import com.daifukuamerica.wrxj.util.SKDCUtility;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

public class DBExport extends SKDCInternalFrame {
	private final String RUN_DIAG = "RUN_EXPORT";
	private final String CLEAR = "CLEAR_SELECTIONS";
	private final String SELECTALL = "SELECT ALL";

	private JPanel northPanel;
	private SKDCButton btnRunDiagnostic;
	private SKDCButton btnClear;
	private SKDCButton btnSelectAll;
	private ActionListener btnEventListener;
	private JCheckBox checkBoxASRSMetaData;
	private JCheckBox checkBoxCarrier;
	private JCheckBox checkBoxContainerType;
	private JCheckBox checkBoxControllerConfig;
	private JCheckBox checkBoxCustomer;
	private JCheckBox checkBoxDedicatedLocation;
	private JCheckBox checkBoxDevice;
	private JCheckBox checkBoxEmployee;
	private JCheckBox checkBoxHostAccess;
	private JCheckBox checkBoxHostConfig;
	private JCheckBox checkBoxHosttoWrx;
	private JCheckBox checkBoxItemMaster;
	private JCheckBox checkBoxJVMConfig;
	private JCheckBox checkBoxLoad;
	private JCheckBox checkBoxLoadLineItem;
	private JCheckBox checkBoxLocation;
	private JCheckBox checkBoxLogin;
	private JCheckBox checkBoxMove;
	private JCheckBox checkBoxOrderHeader;
	private JCheckBox checkBoxOrderLine;
	private JCheckBox checkBoxPort;
	private JCheckBox checkBoxPurchaseOrderHeader;
	private JCheckBox checkBoxPurchaseOrderLine;
	private JCheckBox checkBoxReasonCode;
	private JCheckBox checkBoxRole;
	private JCheckBox checkBoxRoleOption;
	private JCheckBox checkBoxRoute;
	private JCheckBox checkBoxStation;
	private JCheckBox checkBoxSynonyms;
	private JCheckBox checkSysConfig;
	private JCheckBox checkBoxTransActionHistory;
	private JCheckBox checkBoxVehicleMove;
	private JCheckBox checkBoxVehicleSystemCMD;
	private JCheckBox checkBoxWarehouse;
	private JCheckBox checkBoxWrxSequence;
	private JCheckBox checkBoxZone;
	private JCheckBox checkBoxZoneGroup;

	
	protected JTextPane mpTextArea;

	protected String msResults;

	static String OracleDirectory = ("./sql/dbinit/oracle");	
	static String SQLDirectory = ("./sql/dbinit/tsql");
	static String DataBaseName = "";
	static String DataBaseType = "oracle";
	protected String SQLHeader = "";
    protected static String SQLBeginTransaction =""+ System.getProperty("line.separator") + " BEGIN TRANSACTION;"+ System.getProperty("line.separator");
    protected static String SQLEndTransaction = ""+ System.getProperty("line.separator") + "Commit transaction;";
    protected static String SQLGo = ""+ System.getProperty("line.separator") + "Go";
	protected static String SQLFooter = "COMMIT TRANSACTION;"
			+ System.getProperty("line.separator") + " go" +
			 System.getProperty("line.separator");
	protected static String OracleHeader = 
			     "SET DEFINE OFF;"+ System.getProperty("line.separator")
			 + "--SQL Statement which produced this data: "+ System.getProperty("line.separator")
			 + " --"+ System.getProperty("line.separator")
			 + "--  SELECT * FROM ASRS.LOADLINEITEM;" + System.getProperty("line.separator")
	         + " --"+ System.getProperty("line.separator");
	static String OracleFooter  = "COMMIT;" +  System.getProperty("line.separator");

	public DBExport(String isTitle, boolean izResizable, boolean izClosable) {
		super(isTitle, izResizable, izClosable);
		// TODO Auto-generated constructor stub
	}

	public DBExport(String isTitle) {
		super(isTitle);
		// TODO Auto-generated constructor stub
	}

	public DBExport() {
		// TODO Auto-generated constructor stub

		super("DB Table Export");
		setMaximizable(true);

		initInputArea();
	
		defineButtons();
		Container cp = getContentPane();
		cp.add(buildInputPanel(), BorderLayout.NORTH);
		cp.add(new JScrollPane(mpTextArea), BorderLayout.CENTER);
		cp.add(buildButtonPanel(), BorderLayout.SOUTH);
		
		DataBaseType =  Application.getString("database");
				
		DBMetaData vpDBMetaData = Factory.create(DBMetaData.class);
      	   DataBaseName=  vpDBMetaData.getDatabaseInstanceName();
      	 SQLHeader=	 "use  [" + DataBaseName + "];" +" "+	SQLGo +" "+  SQLBeginTransaction;
	}

	/**
	 * 
	 * getPreferredSize
	 */
	@Override
	public Dimension getPreferredSize() {
		return (new Dimension(750, 420));
	}

	/*
	 * ==========================================================================
	 * =***** Event Listeners go here ******
	 * ====================================
	 * =======================================
	 */
	/**
	 * Defines all buttons on the main Host frame view, and adds listeners to
	 * them.
	 */
	private void setButtonListeners() {
		btnEventListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String which_button = e.getActionCommand();
				if (which_button.equals(RUN_DIAG)) {
					runDiagnostic();
				}
				if (which_button.equals(CLEAR)) {
					runClear();
				}
				if (which_button.equals(SELECTALL)) {
					runSelectAll();
				}
			}
		};
		// Attach listeners.
		btnRunDiagnostic.addEvent(RUN_DIAG, btnEventListener);
		btnClear.addEvent(CLEAR, btnEventListener);
		btnSelectAll.addEvent(SELECTALL, btnEventListener);
	}

	/*
	 * ==========================================================================
	 * =***** Button pressed action methods go here ******
	 * ======================
	 * =====================================================
	 */
	public void runClear() {

		String sText = "";
		mpTextArea.setText(sText);
		checkBoxASRSMetaData.setSelected(false);
	
		checkBoxCarrier.setSelected(false);
		checkBoxContainerType.setSelected(false);
		checkBoxControllerConfig.setSelected(false);
		checkBoxCustomer.setSelected(false);
		checkBoxDedicatedLocation.setSelected(false);
		checkBoxDevice.setSelected(false);
		checkBoxEmployee.setSelected(false);
		checkBoxHostAccess.setSelected(false);
		checkBoxHostConfig.setSelected(false);
		checkBoxHosttoWrx.setSelected(false);
		checkBoxItemMaster.setSelected(false);
		checkBoxJVMConfig.setSelected(false);
		checkBoxLoad.setSelected(false);
		checkBoxLoadLineItem.setSelected(false);
		checkBoxLocation.setSelected(false);
		checkBoxLogin.setSelected(false);
		checkBoxMove.setSelected(false);
		checkBoxOrderHeader.setSelected(false);
		checkBoxOrderLine.setSelected(false);
		checkBoxPort.setSelected(false);
		checkBoxPurchaseOrderHeader.setSelected(false);
		checkBoxPurchaseOrderLine.setSelected(false);
		checkBoxReasonCode.setSelected(false);
		checkBoxRole.setSelected(false);
		checkBoxRoleOption.setSelected(false);
		checkBoxRoute.setSelected(false);
		checkBoxStation.setSelected(false);
		checkBoxSynonyms.setSelected(false);
		checkSysConfig.setSelected(false);
		checkBoxTransActionHistory.setSelected(false);
		checkBoxVehicleMove.setSelected(false);
		checkBoxVehicleSystemCMD.setSelected(false);
		checkBoxWarehouse.setSelected(false);
		checkBoxWrxSequence.setSelected(false);
		checkBoxZone.setSelected(false);
		checkBoxZoneGroup.setSelected(false);

	}

	public void runSelectAll() {

		String sText = "";
		mpTextArea.setText(sText);
		checkBoxASRSMetaData.setSelected(true);
		checkBoxCarrier.setSelected(true);
		checkBoxContainerType.setSelected(true);
		checkBoxControllerConfig.setSelected(true);
		checkBoxCustomer.setSelected(true);
		checkBoxDedicatedLocation.setSelected(true);
		checkBoxDevice.setSelected(true);
		checkBoxEmployee.setSelected(true);
		checkBoxHostAccess.setSelected(true);
		checkBoxHostConfig.setSelected(true);
		checkBoxHosttoWrx.setSelected(true);
    	checkBoxItemMaster.setSelected(true);
		checkBoxJVMConfig.setSelected(true);
		checkBoxLoad.setSelected(true);
		checkBoxLoadLineItem.setSelected(true);
		checkBoxLocation.setSelected(true);
		checkBoxLogin.setSelected(true);
		checkBoxMove.setSelected(true);
		checkBoxOrderHeader.setSelected(true);
		checkBoxOrderLine.setSelected(true);
		checkBoxPort.setSelected(true);
		checkBoxPurchaseOrderHeader.setSelected(true);
		checkBoxPurchaseOrderLine.setSelected(true);
		checkBoxReasonCode.setSelected(true);
		checkBoxRole.setSelected(true);
		checkBoxRoleOption.setSelected(true);
		checkBoxRoute.setSelected(true);
		checkBoxStation.setSelected(true);
		checkBoxSynonyms.setSelected(true);
		checkSysConfig.setSelected(true);
	//	checkBoxTransActionHistory.setSelected(true);
		checkBoxVehicleMove.setSelected(true);
		checkBoxVehicleSystemCMD.setSelected(true);
		checkBoxWarehouse.setSelected(true);
		checkBoxWrxSequence.setSelected(true);
		checkBoxZone.setSelected(true);
		checkBoxZoneGroup.setSelected(true);
	
	}

	public void runDiagnostic() {
		initializeTextArea();
		if (checkBoxASRSMetaData.isSelected()) {
			ASRSMetaData(OracleDirectory, SQLDirectory);
		}
		if (checkBoxCarrier.isSelected()) {
			Carrier(OracleDirectory, SQLDirectory);
		}
		if (checkBoxContainerType.isSelected()) {
			ContainerType(OracleDirectory, SQLDirectory);
		}
		if (checkBoxControllerConfig.isSelected()) {
			ControllerConfig(OracleDirectory, SQLDirectory);
		}
		if (checkBoxCustomer.isSelected()) {
			Customer(OracleDirectory, SQLDirectory);
		}
		if (checkBoxDedicatedLocation.isSelected()) {
			DedicatedLocation(OracleDirectory, SQLDirectory);
		}
		if (checkBoxDevice.isSelected()) {
			Device(OracleDirectory, SQLDirectory);
		}
		if (checkBoxEmployee.isSelected()) {
			Employee(OracleDirectory, SQLDirectory);
		}
		if (checkBoxHostAccess.isSelected()) {
			HostOutAccess(OracleDirectory, SQLDirectory);
		}
		if (checkBoxHostConfig.isSelected()) {
			HostConfig(OracleDirectory, SQLDirectory);
		}
		// checkBoxHosttoWrx.setSelected(true);
		if (checkBoxItemMaster.isSelected()) {
			ItemMaster(OracleDirectory, SQLDirectory);
		}
		if (checkBoxJVMConfig.isSelected()) {
			JVMConfig(OracleDirectory, SQLDirectory);
		}
		if (checkBoxLoad.isSelected()) {
			Load(OracleDirectory, SQLDirectory);
		}
		if (checkBoxLoadLineItem.isSelected()) {
			LoadLineItem(OracleDirectory, SQLDirectory);
		}
		if (checkBoxLocation.isSelected()) {
			Location(OracleDirectory, SQLDirectory);
		}
		if (checkBoxLogin.isSelected()) {
			Login(OracleDirectory, SQLDirectory);
		}
		if (checkBoxMove.isSelected()) {
			Move(OracleDirectory, SQLDirectory);
		}
		if (checkBoxOrderHeader.isSelected()) {
			OrderHeader(OracleDirectory, SQLDirectory);
		}
		if (checkBoxOrderLine.isSelected()) {
			OrderLine(OracleDirectory, SQLDirectory);
		}
		if (checkBoxPort.isSelected()) {
			Port(OracleDirectory, SQLDirectory);
		}
		if (checkBoxPurchaseOrderHeader.isSelected()) {
			OrderHeader(OracleDirectory, SQLDirectory);
		}
		if (checkBoxPurchaseOrderLine.isSelected()) {
			OrderLine(OracleDirectory, SQLDirectory);
		}
		if (checkBoxPurchaseOrderHeader.isSelected()) {
			PurchaseOrderHeader(OracleDirectory, SQLDirectory);
		}
		if (checkBoxPurchaseOrderLine.isSelected()) {
			PurchaseOrderLine(OracleDirectory, SQLDirectory);
		}
		if (checkBoxRole.isSelected()) {
			Role(OracleDirectory, SQLDirectory);
		}
		if (checkBoxRoleOption.isSelected()) {
			RoleOption(OracleDirectory, SQLDirectory);
		}
		if (checkBoxRoute.isSelected()) {
			Route(OracleDirectory, SQLDirectory);
		}
		if (checkBoxStation.isSelected()) {
			Station(OracleDirectory, SQLDirectory);
		}
		if (checkBoxSynonyms.isSelected()) {
			Synonyms(OracleDirectory, SQLDirectory);
		}
		if (checkSysConfig.isSelected()) {
			SysConfig(OracleDirectory, SQLDirectory);
		}
	//	if (checkBoxTransActionHistory.isSelected()) {
			//   TransActionHistory(OracleDirectory, SQLDirectory);
//		}
		if (checkBoxVehicleMove.isSelected()) {
			VehicleMove(OracleDirectory, SQLDirectory);
		}
		if (checkBoxVehicleSystemCMD.isSelected()) {
			VehicleSystemCMD(OracleDirectory, SQLDirectory);
		}
		if (checkBoxWarehouse.isSelected()) {
			Warehouse(OracleDirectory, SQLDirectory);
		}
		if (checkBoxWrxSequence.isSelected()) {
			WrxSequence(OracleDirectory, SQLDirectory);
		}
		if (checkBoxZone.isSelected()) {
			Zone(OracleDirectory, SQLDirectory);
		}
		if (checkBoxZoneGroup.isSelected()) {
			ZoneGroup(OracleDirectory, SQLDirectory);
		}
	}

	/**
	 * Initialize the text area
	 */
	protected void initializeTextArea() {
		mpTextArea.setText("");

		msResults = "<html>";
	}

	/**
	 * Add a header
	 * 
	 * @param isHeader
	 */

	/*
	 * ==========================================================================
	 * =***** All other private methods go here ******
	 * ==========================
	 * =================================================
	 */
	private void defineButtons() {
		btnSelectAll = new SKDCButton("Select All", "Select All", 'R');

		btnClear = new SKDCButton("Clear Selections", "Clear Selections", 'R');

		btnRunDiagnostic = new SKDCButton("Run Export",
				"Execute the export tool.", 'R');
		setButtonListeners();
	}

	private void initInputArea() {
		mpTextArea = new JTextPane();
		mpTextArea.setContentType("text/html");
		mpTextArea.setEditable(false);
		checkBoxASRSMetaData = new JCheckBox("ASRSMetaData", false);
		checkBoxCarrier = new JCheckBox("Carrier ", false);
		checkBoxContainerType = new JCheckBox("ContainerType", false);
		checkBoxControllerConfig = new JCheckBox("Controller Config", false);
		checkBoxCustomer = new JCheckBox("Customer ", false);
		checkBoxDedicatedLocation = new JCheckBox("DedicatedLocation", false);
		checkBoxDevice = new JCheckBox("Device", false);
		checkBoxEmployee = new JCheckBox("Employee", false);
		checkBoxHostAccess = new JCheckBox("Host Access ", false);
		checkBoxHostConfig = new JCheckBox("Host Config ", false);
		checkBoxHosttoWrx = new JCheckBox("HosttoWrx", false);
		checkBoxJVMConfig = new JCheckBox("JVMConfig", false);
		checkBoxLogin = new JCheckBox("Login", false);
		checkBoxMove = new JCheckBox("Move", false);
		checkBoxOrderHeader = new JCheckBox("Order Header", false);
		checkBoxOrderLine = new JCheckBox("Order Line", false);
		checkBoxPort = new JCheckBox("Port", false);
		checkBoxPurchaseOrderHeader = new JCheckBox("OrderHeader", false);
		checkBoxPurchaseOrderLine = new JCheckBox("Purchase Order Line", false);
		checkBoxReasonCode = new JCheckBox("Reason Code", false);
		checkBoxRole = new JCheckBox("Role", false);
		checkBoxRoleOption = new JCheckBox("Role Option", false);
		checkBoxRoute = new JCheckBox("Route", false);
		checkBoxStation = new JCheckBox("Station", false);
		checkBoxSynonyms = new JCheckBox("Synonym", false);
		checkSysConfig = new JCheckBox("SysConfig", false);
//		checkBoxTransActionHistory = new JCheckBox("TransActionHistory", false);
		checkBoxVehicleMove = new JCheckBox("Vehicle Move", false);
		checkBoxVehicleSystemCMD = new JCheckBox("Vehicle System CMD", false);
		checkBoxWarehouse = new JCheckBox("Warehouse", false);
		checkBoxWrxSequence = new JCheckBox("WrxSequence", false);
		checkBoxZone = new JCheckBox("Zone", false);
		checkBoxZoneGroup = new JCheckBox("Zone Group", false);

		checkBoxLocation = new JCheckBox("Locations", false);
		checkBoxLoad = new JCheckBox("Load", false);
		checkBoxItemMaster = new JCheckBox("ItemMaster", false);
		checkBoxLoadLineItem = new JCheckBox("Items", false);
	}

	/**
	 * Define input panel components.
	 * 
	 * @return Built JPanel with input text boxes.
	 */
	private JPanel buildInputPanel() {
		northPanel = new JPanel();
		northPanel = getEmptyInputPanel("");
		// Create Grid bag constraints.
		GridBagConstraints gbconst = new GridBagConstraints();

		gbconst.insets = new Insets(1, 1, 5, 1);
		gbconst.gridy = 1;
		gbconst.gridwidth = 1;
		gbconst.weighty = 0.5;
		gbconst.weightx = 5;
		gbconst.gridx = GridBagConstraints.RELATIVE;
		gbconst.anchor = GridBagConstraints.WEST;
		
		northPanel.add(checkBoxWarehouse, gbconst);
		northPanel.add(checkBoxLocation, gbconst);
		northPanel.add(checkBoxLoad, gbconst);
		northPanel.add(checkBoxItemMaster, gbconst);
		northPanel.add(checkBoxLoadLineItem, gbconst);
		
		gbconst.gridy = 2;
		gbconst.anchor = GridBagConstraints.WEST;

		northPanel.add(checkBoxEmployee, gbconst);
		northPanel.add(checkBoxLogin, gbconst);
		northPanel.add(checkBoxRole, gbconst);
		northPanel.add(checkBoxRoleOption, gbconst);

		gbconst.gridy = 3;
		
//		northPanel.add(checkBoxTransActionHistory, gbconst);
		northPanel.add(checkBoxStation, gbconst);
		northPanel.add(checkBoxRoute, gbconst);
		northPanel.add(checkBoxDevice, gbconst);
		northPanel.add(checkBoxPort, gbconst);
	
		
		gbconst.gridy = 4;
	
//		northPanel.add(checkBoxDedicatedLocation, gbconst);
	
		northPanel.add(checkBoxHostAccess, gbconst);
		northPanel.add(checkBoxHostConfig, gbconst);
		northPanel.add(checkBoxContainerType, gbconst);
		northPanel.add(checkSysConfig, gbconst);
		northPanel.add(checkBoxControllerConfig, gbconst);
		
			
		gbconst.gridy = 5;
		
//		northPanel.add(checkBoxDedicatedLocation, gbconst);
		northPanel.add(checkBoxOrderHeader, gbconst);
		northPanel.add(checkBoxOrderLine, gbconst);
		northPanel.add(checkBoxMove, gbconst);
		northPanel.add(checkBoxCustomer, gbconst);	
		
		return (northPanel);
	}

	/**
	 * Define the button panel at the bottom of the screen.
	 * 
	 * @return JPanel with buttons in place.
	 */
	private JPanel buildButtonPanel() {
		JPanel buttonPanel = getEmptyButtonPanel();

		buttonPanel.add(btnSelectAll);
		buttonPanel.add(btnClear);
		buttonPanel.add(btnRunDiagnostic);

		return (buttonPanel);
	}

	public void ASRSMetaData(String OracleDirectory, String SqlDirectory) 
	{
		AsrsMetaDataData mpAsrsMeteData = Factory.create(AsrsMetaDataData.class);
		String vsTableName = "AsrsMetaData";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ " (SDATAVIEWNAME, SCOLUMNNAME, SFULLNAME, SISTRANSLATION,  IDISPLAYORDER)"
				+ "values " + System.getProperty("line.separator");
		mpAsrsMeteData.setOrderByColumns(AsrsMetaDataData.COLUMNNAME_NAME);
		try {
			
//			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
//			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
					
			List<Map> vpMapList;
			AsrsMetaData vpAsrsData = Factory.create(AsrsMetaData.class);
			vpMapList = vpAsrsData.getAllElements(mpAsrsMeteData);
			for (Map vpMap : vpMapList) {
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpAsrsMeteData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpAsrsMeteData.getDataViewName().trim() + "', "
						+ "'" + mpAsrsMeteData.getColumnName().trim() + "', " 
						+ "'" + mpAsrsMeteData.getFullName().trim() + "', "
						+ "'" +  mpAsrsMeteData.getIsTranslation().trim() + "', "
						+ mpAsrsMeteData.getDisplayOrder()+ ");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void Carrier(String OracleDirectory, String SqlDirectory)
	{
		
	}
	public void ContainerType(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "ContainerType";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ " (SCONTAINERTYPE, FWEIGHT, FMAXWEIGHT, FCONTLENGTH,"
				+ " FCONTWIDTH, FCONTHEIGHT)"
    		    + " Values"
				+ System.getProperty("line.separator");

		ContainerType mpContainerType = Factory.create(ContainerType.class);
		ContainerTypeData mpContainerTypeData = Factory.create(ContainerTypeData.class);
		mpContainerTypeData.setOrderByColumns(ContainerTypeData.CONTAINERTYPE_NAME);
		try
		{
//			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
//			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;

			vpMapList = mpContainerType.getAllElements(mpContainerTypeData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpContainerTypeData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpContainerTypeData.getContainer().trim()	+ "',"
						                    +  mpContainerTypeData.getWeight() +","
				                			+  mpContainerTypeData.getMaxWeight() +","
							            	+  mpContainerTypeData.getContLength() +","
									        +  mpContainerTypeData.getContWidth() +","
								     		+  mpContainerTypeData.getContHeight()	+ ");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void BoxContainerType(String OracleDirectory, String SqlDirectory) {
	}

	public void ControllerConfig(String OracleDirectory, String SqlDirectory)
	{
		   String vsTableName = "ControllerConfig";
		   String vpTableHeader = "	INSERT INTO asrs."+ vsTableName	
					+ "(SCONTROLLER, SPROPERTYNAME, SPROPERTYVALUE, SPROPERTYDESC, "
					+ "ISCREENCHANGEALLOWED, IENABLED)"
					+ " values "
					+ System.getProperty("line.separator");
		
		   ControllerConfig mpControllerConfig = Factory.create(ControllerConfig.class);
		   ControllerConfigData mpControllerConfigData = Factory.create(ControllerConfigData.class);
		   mpControllerConfigData.setOrderByColumns(ControllerConfigData.CONTROLLER_NAME);
		try 
		{
//			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
//			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
		
			vpMapList = mpControllerConfig.getAllElements(mpControllerConfigData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpControllerConfigData.dataToSKDCData(vpMap);
				String importText = "";

					importText = "('" + mpControllerConfigData.getController().trim() + "'," 
							+ "'" + mpControllerConfigData.getPropertyName().trim()+ "'," 
							+ "'" + mpControllerConfigData.getPropertyValue().trim()+ "'," 
							+ "'" + mpControllerConfigData.getPropertyDesc().trim()+ "'," 
							+ mpControllerConfigData.getScreenChangeAllowed()+ ","
							+ mpControllerConfigData.getEnabled() + ")"
							+ System.getProperty("line.separator");
					writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
				}
				writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}
		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void Customer(String OracleDirectory, String SqlDirectory) {
	
			String vsTableName = "customer";
			String vpTableHeader = "INSERT INTO asrs." + vsTableName 
		                                                + "(sCustomer, sDescription1, sDescription2, "
		                                                + "sStreetAddress1, sStreetAddress2, sCity, "
		                                                + " sState, sZipcode, sCountry, sPhone, "
		                                                + "sAttention, sContact,sNote, "		                                                
		                                                + "iDeleteOnUse) "
		                                            	+ " values "
		                                                + System.getProperty("line.separator");
			
			Customer mpCustomer = Factory.create(Customer.class);
			CustomerData mpCustomerData = Factory.create(CustomerData.class);
			mpCustomerData.setOrderByColumns(CustomerData.CUSTOMER_NAME);
			try {
				deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
				deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
				writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
				writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
				writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
				List<Map> vpMapList;
				vpMapList = mpCustomer.getAllElements(mpCustomerData);
				for (Map vpMap : vpMapList) {
					writeTextFile(SqlDirectory, vsTableName+ ".tsql", vpTableHeader);
					mpCustomerData.dataToSKDCData(vpMap);
					String importText = "";
					importText = "('" + mpCustomerData.getCustomer().trim() + "', "
							+ "'" + mpCustomerData.getDescription1() + "', "
							+ "'" + mpCustomerData.getDescription2() + "', "
							+ "'" + mpCustomerData.getStreetAddress1() + "', "
							+ "'" + mpCustomerData.getStreetAddress2() + "', "
							+ "'" + mpCustomerData.getCity() + "', "
							+ "'" + mpCustomerData.getState() + "', "
							+ "'" + mpCustomerData.getZipcode() + "', "
							+ "'" + mpCustomerData.getCountry() + "', "
							+ "'" + mpCustomerData.getPhone() + "', "
							+ "'" + mpCustomerData.getAttention() + "', "
							+ "'" + mpCustomerData.getContact() + "', "
							+ "'" + mpCustomerData.getNote() + "', "
						    + mpCustomerData.getDeleteOnUse() + ");"
							+ System.getProperty("line.separator");
					writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
				}
				writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
			}

			catch (DBException e) {
				throw new RuntimeException("IO Error occured");
			}
		}
		

	public void DedicatedLocation(String OracleDirectory, String SqlFileName) {
	}
	public void Device(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "device";
		String vpTableHeader = "INSERT INTO asrs." + vsTableName 
	                                                + "( SDEVICEID, IDEVICETYPE, IAISLEGROUP, "
	                                                + "SCOMMDEVICE, IOPERATIONALSTATUS, "
	                                                + "IPHYSICALSTATUS, IEMULATIONMODE, "
	                                                + "SCOMMSENDPORT, SCOMMREADPORT, "
	                                                + "SERRORCODE, SNEXTDEVICE, "
	                                                + "IDEVICETOKEN, SSCHEDULERNAME, "
	                                                + "SALLOCATORNAME, SSTATIONNAME, "
	                                                +  "SUSERID, SPRINTER, SWAREHOUSE, "
	                                                + " SJVMIDENTIFIER)"
	                                                + "VALUES"
			                                    	+ System.getProperty("line.separator");
		
		Device mpDevice = Factory.create(Device.class);
		DeviceData mpDeviceData = Factory.create(DeviceData.class);
		mpDeviceData.setOrderByColumns(DeviceData.DEVICEID_NAME);
		try {
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			List<Map> vpMapList;
			vpMapList = mpDevice.getAllElements(mpDeviceData);
			for (Map vpMap : vpMapList) {
				writeTextFile(SqlDirectory, vsTableName+ ".tsql", vpTableHeader);
				mpDeviceData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "('" + mpDeviceData.getDeviceID().trim() + "', "
						+ mpDeviceData.getDeviceType() + ", "
						+ mpDeviceData.getAisleGroup() + ","
						+ "'" + mpDeviceData.getCommDevice().trim() + "', "
						+ mpDeviceData.getOperationalStatus() + ","
						+ mpDeviceData.getPhysicalStatus() + ","
						+ mpDeviceData.getEmulationMode() + "," 
						+ "'" + mpDeviceData.getCommSendPort().trim() + "', "
						+ "'" + mpDeviceData.getCommReadPort().trim() + "', "
						+ "'" + mpDeviceData.getErrorCode().trim() + "', "
						+ "'" + mpDeviceData.getNextDevice().trim() + "', "
						+ mpDeviceData.getDeviceToken() + ","
						+ "'" + mpDeviceData.getSchedulerName().trim() + "', "
						+ "'" + mpDeviceData.getAllocatorName().trim() + "', "
						+ "'" + mpDeviceData.getStationName().trim() + "', "
						+ "'" + mpDeviceData.getUserID().trim() + "', "
						+ "'" + mpDeviceData.getPrinter().trim() + "', "
						+ "'"  + mpDeviceData.getWarehouse().trim() + "', "
						+ "'" + mpDeviceData.getJVMIdentifier().trim() + "');"
						
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	
	public void Employee(String OracleDirectory, String SqlDirectory)
	{
		Employee mpEmployee = Factory.create(Employee.class);
		String vsTableName = "Employee";
			
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName  
				+ " (SUSERID, SUSERNAME, SROLE, SPASSWORD, SRELEASETOCODE,"
				+ " SLANGUAGE)"
				+"  VALUES"
				+ System.getProperty("line.separator");
	
		
		EmployeeData mpEmployeeData = Factory.create(EmployeeData.class);
		mpEmployeeData.setOrderByColumns(EmployeeData.USERNAME_NAME);
		try
        {
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
		    vpMapList = mpEmployee.getAllElements(mpEmployeeData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql", vpTableHeader);
				mpEmployeeData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpEmployeeData.getUserID().trim() + "', "
						+ "'" + mpEmployeeData.getUserName().trim() + "', "
						+ "'" + mpEmployeeData.getRole().trim() + "', " 
						+ "'" + mpEmployeeData.getPassword().trim() + "', "
						+ "'" +  mpEmployeeData.getReleaseToCode().trim() + "', "
						+ "'" + mpEmployeeData.getLanguage().trim()+ "'); "
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLEndTransaction);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void HostConfig(String OracleDirectory, String SqlDirectory) 
	{
		   String vsTableName = "HostConfig";
		   String vpTableHeader = "	INSERT INTO asrs."+ vsTableName	
				  + " (SDATAHANDLER, SGROUP, SPARAMETERNAME, "
				  + "SPARAMETERVALUE, IACTIVECONFIG)"
				   + "Values"
					+ System.getProperty("line.separator");
		
		   HostConfig mpHostConfig = Factory.create(HostConfig.class);
		   HostConfigData mpHostConfigData = Factory.create(HostConfigData.class);
		   mpHostConfigData.setOrderByColumns(HostConfigData.DATAHANDLER_NAME);
		try 
		{
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
		
			vpMapList = mpHostConfig.getAllElements(mpHostConfigData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpHostConfigData.dataToSKDCData(vpMap);
				String importText = "";

					importText = "('" + mpHostConfigData.getDataHandler().trim() + "',"
							+ "'" + mpHostConfigData.getGroup().trim()+ "'," 
							+ "'" + mpHostConfigData.getParameterName().trim()+ "'," 
								+ "'" + mpHostConfigData.getParameterValue().trim()+ "'," 
							+ mpHostConfigData.getActiveConfig() +");"
							+ System.getProperty("line.separator");
					writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
				}
				writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void HostOutAccess(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "HostOutAccess";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ " (SHOSTNAME, SMESSAGEIDENTIFIER, IENABLED)" + "Values"
				+ System.getProperty("line.separator");

		HostOutAccess mpHostOutAccess = Factory.create(HostOutAccess.class);
		HostOutAccessData mpHostOutAccessData = Factory
				.create(HostOutAccessData.class);
		mpHostOutAccessData.setOrderByColumns(HostOutAccessData.HOSTNAME_NAME);
		try
		{
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;

			vpMapList = mpHostOutAccess.getAllElements(mpHostOutAccessData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpHostOutAccessData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpHostOutAccessData.getHostName().trim()	+ "',"
			         	+ "'"	+ mpHostOutAccessData.getMessageIdentifier().trim() + "',"
						+  mpHostOutAccessData.getEnabled() + ");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void HosttoWrx(String OracleDirectory, String SqlDirectory) {
	}

	public void ItemMaster(String OracleDirectory, String SqlDirectory) 
	{
		String vsTableName = "ItemMaster";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName + 
                                             " (SITEM, SDESCRIPTION, SRECOMMENDEDWAREHOUSE, IHOLDTYPE, IDELETEATZEROQUANTITY, DLASTCCIDATE, IPIECESPERUNIT," + 
		                                      " FCCIPOINTQUANTITY, FDEFAULTLOADQUANTITY, FITEMWEIGHT, FITEMLENGTH, FITEMHEIGHT, FITEMWIDTH, FCASEWEIGHT," +
	                                     	  " FCASELENGTH, FCASEHEIGHT, FCASEWIDTH, IEXPIRATIONREQUIRED, ISTORAGEFLAG) VALUES" + 
				                                 System.getProperty("line.separator");
		
		ItemMaster mpIM = Factory.create(ItemMaster.class);
			ItemMasterData mpIMData;
		mpIMData = Factory.create(ItemMasterData.class);
		mpIMData.setOrderByColumns(ItemMasterData.ITEM_NAME);

		try
		{
		    ItemMasterData vpIMD = Factory.create(ItemMasterData.class);
			deletefile(OracleDirectory  +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + "orderline" + ";" + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + "orderheader" + ";" + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + "loadlineitem" + ";" + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + "Load" + ";" + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + "location where ilocationtype != 10" + ";" + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			vpIMD.addOrderByColumn(ItemMasterData.ITEM_NAME);
		    vpMapList = mpIM.getAllElements(vpIMD);
		    int viLineCnt = 0;
			for (Map vpMap : vpMapList) {
				viLineCnt ++;
				if (viLineCnt % 100  == 0)
					writeTextFile(SqlDirectory, vsTableName+".tsql", 	SQLEndTransaction +  SQLGo+ SQLBeginTransaction);
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				vpIMD = Factory.create(ItemMasterData.class);
				vpIMD.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + vpIMD.getItem().trim() + "', "
						+ "'"+ vpIMD.getDescription().trim() + "', " 
						+ "'"+ vpIMD.getRecommendedWarehouse().trim() + "', "
						+ vpIMD.getHoldType() + ", "
						+ vpIMD.getDeleteAtZeroQuantity() + ", "
						+ " '" + vpIMD.getLastCCIDate()+"', "
						+ vpIMD.getPiecesPerUnit() + ", "
						+ vpIMD.getCCIPointQuantity() + ", "
						+ vpIMD.getDefaultLoadQuantity() + ", "
						+ vpIMD.getWeight() + ", "
						+ vpIMD.getItemLength() + ", "
					    + vpIMD.getItemHeight() + ", "
						+ vpIMD.getItemWidth() + ", "
					    + vpIMD.getCaseWeight() + ", "
						+ vpIMD.getCaseLength() + ", "
						+ vpIMD.getCaseHeight() + ", "
						+ vpIMD.getCaseWidth()  + ", "
						+ vpIMD.getExpirationRequired() + ","
						+ vpIMD.getStorageFlag() + ");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLEndTransaction + SQLFooter);
		}
		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void JVMConfig(String OracleDirectory, String SqlDirectory) {
	}
/*
 *  read and create load import file.	
 */
	protected  void Load(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "Load";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ "(SPARENTLOAD, SLOADID, SWAREHOUSE, SADDRESS, SCONTAINERTYPE, ILOADMOVESTATUS, DMOVEDATE, ILOADPRESENCECHECK, "
				+ "IHEIGHT, SDEVICEID, IAMOUNTFULL, FWEIGHT, SMCKEY, SBCRDATA)  Values"
				+ System.getProperty("line.separator");
	
		StandardLoadServer mpLoadServer;
		mpLoadServer = Factory.create(StandardLoadServer.class);

		LoadData mpLDData;
		mpLDData = Factory.create(LoadData.class);
		mpLDData.setOrderByColumns(LoadData.LOADID_NAME);
		FileReader file = null;
		mpLDData.addOrderByColumn(LoadData.LOADID_NAME);
		try {
			List<Map> vpLDList = mpLoadServer.getLoadDataList(mpLDData);
			if (vpLDList == null || vpLDList.isEmpty()) {
				return;
			}
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			for (int viLineCnt = 0; viLineCnt < vpLDList.size(); viLineCnt++)
			{
				if (viLineCnt % 100  == 0)
						writeTextFile(SqlDirectory, vsTableName+".tsql", 	SQLEndTransaction + SQLGo+ SQLBeginTransaction);
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpLDData.clear();
				String importText = "";
				mpLDData.dataToSKDCData(vpLDList.get(viLineCnt));
			
				importText = "('" + mpLDData.getParentLoadID().trim() + "', "
						+ "'" + mpLDData.getLoadID().trim() + "', " + "'"
						+ mpLDData.getWarehouse().trim() + "', " + "'"
						+ mpLDData.getAddress().trim() + "', " 
						+ "'" + mpLDData.getContainerType().trim() + "', "
						+ mpLDData.getLoadMoveStatus() + ","
						+ " '" + mpLDData.getMoveDate()+ "',"
				        + mpLDData.getLoadPresenceCheck() + ", "
						+ mpLDData.getHeight() + ", " + "'"
						+ mpLDData.getDeviceID() + "', "
						+ mpLDData.getAmountFull() + ", "
						+ mpLDData.getWeight() +","
						+ "'" + mpLDData.getMCKey().trim() + "',"
						+ "'" + mpLDData.getBCRData().trim() + "');"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void LoadLineItem(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "LoadLineItem";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ "(SITEM, SLOT, SPOSITIONID,  SLOADID, DLASTCCIDATE, DAGINGDATE,  DEXPIRATIONDATE, "
				+ "FCURRENTQUANTITY, FALLOCATEDQUANTITY, IHOLDTYPE, IPRIORITYALLOCATION, "
				+ "SEXPECTEDRECEIPT)  Values"
				+ System.getProperty("line.separator");
		
		StandardInventoryServer mpInvServer;
		mpInvServer = Factory.create(StandardInventoryServer.class);

		LoadLineItemData mpLLiData;
		mpLLiData = Factory.create(LoadLineItemData.class);
		mpLLiData.setOrderByColumns(LoadLineItemData.ITEM_NAME);
		
	    try
		{
				
			
			List<Map> vpLLiList = mpInvServer
					.getLoadLineItemDataList(mpLLiData);
			if (vpLLiList == null )
			{	
				return;
			}
	    	if ( vpLLiList.isEmpty())
			{	
				return;
			}	
		
			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));

			for (int viLineCnt = 0; viLineCnt < vpLLiList.size(); viLineCnt++)
			{
				if (viLineCnt % 100  == 0)
					writeTextFile(SqlDirectory, vsTableName+".tsql", 	SQLEndTransaction + SQLGo+ SQLBeginTransaction);
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpLLiData.clear();
				String importText = "";
				String vslot;
				mpLLiData.dataToSKDCData(vpLLiList.get(viLineCnt));
				if (SKDCUtility.isFilledIn(mpLLiData.getLot()))
				{
					vslot ="'"+mpLLiData.getLot().trim()+ "'";	
				}
				else
				{	
					 vslot = null;	
				}
				importText = "('" + mpLLiData.getItem().trim() + "',"
						+ vslot + ","
					    + "'" + mpLLiData.getPositionID().trim() + "',"
					    + "'" + mpLLiData.getLoadID().trim() + "',"
						+ "'" + mpLLiData.getLastCCIDate()+ "',"
						+ "'" + mpLLiData.getAgingDate() + "',"
						+ "'" + mpLLiData.getExpirationDate() + "',"
						+ mpLLiData.getCurrentQuantity() + ","
						+ mpLLiData.getAllocatedQuantity() + ","
						+ mpLLiData.getHoldType() + ","
						+ mpLLiData.getPriorityAllocation() + ","
						 + "'" + mpLLiData.getExpectedReceipt().trim() + "');"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLEndTransaction + SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void Location(String OracleDirectory, String SqlDirectory) 
	{
		String vsTableName = "Location";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ " (SWAREHOUSE, SADDRESS, SZONE, ILOCATIONSTATUS, ILOCATIONTYPE, "
			    + " IEMPTYFLAG, SDEVICEID, IHEIGHT, ISEARCHORDER, IAISLEGROUP," 
			    + " IALLOWDELETION, IMOVESEQUENCE, ILOCATIONDEPTH, SLINKEDADDRESS,"
			   	+ " ISWAPZONE, SSHELFPOSITION)"
				+ "  Values"
				+ System.getProperty("line.separator");
		
		StandardLocationServer mpLocationServer;
		mpLocationServer = Factory.create(StandardLocationServer.class);

		LocationData mpLCData;
		mpLCData = Factory.create(LocationData.class);
	//mpLCData.setKey(LocationData.LOCATIONTYPE_NAME,DBConstants.LCASRS);
		mpLCData.setOrderByColumns(LocationData.ADDRESS_NAME);
		FileReader file = null;
		mpLCData.setOrderByColumns(LocationData.ADDRESS_NAME);
		try {
			List<Map> vpLCList = mpLocationServer.getLocationData(mpLCData);
			if (vpLCList == null || vpLCList.isEmpty()) {
				return;
			}
			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
//			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			
			writeTextFile(SqlDirectory, vsTableName+".tsql",	"delete asrs.Loadlineitem ; " + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", "delete asrs.ItemMaster ; " + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql", "delete asrs.Load ; "   + System.getProperty("line.separator"));
			writeTextFile(SqlDirectory, vsTableName+".tsql",	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
	
	 	  
			for (int viLineCnt = 0; viLineCnt < vpLCList.size(); viLineCnt++)
			{
				if (viLineCnt % 100  == 0)
						writeTextFile(SqlDirectory, vsTableName+".tsql", 	SQLEndTransaction + SQLGo+ SQLBeginTransaction);
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpLCData.clear();
				String importText = "";
				mpLCData.dataToSKDCData(vpLCList.get(viLineCnt));
			    importText = 
			    		"('" + mpLCData.getWarehouse().trim() + "',"
                         + "'" + mpLCData.getAddress().trim() + "', "
                           + "'" + mpLCData.getZone().trim() + "', "
						 +mpLCData.getLocationStatus() + ","
						+ mpLCData.getLocationType() + ", " 
						+ mpLCData.getEmptyFlag() + ", " 
						+ "'" +mpLCData.getDeviceID() + "', " 
						 + mpLCData.getHeight() + ", " +
						 + mpLCData.getSearchOrder() + ", " 
						 + mpLCData.getAisleGroup() + ", "
						 + mpLCData.getAllowDeletion() + ", "
						 + mpLCData.getMoveSequence() + ", "
						 + mpLCData.getLocationDepth() + ", " 
					     + "'" + mpLCData.getLinkedAddress().trim() + "', "
						 + mpLCData.getSwapZone() + 	"," 
					     + "'" + mpLCData.getShelfPosition().trim() + "');"
						 + System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLEndTransaction + SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void Login(String OracleDirectory, String SqlDirectory) {
		String vsTableName = "Login";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ "( SUSERID, SROLE, DLOGINTIME, SMACHINENAME, SIPADDRESS )"
				+ "VALUES "
				+ System.getProperty("line.separator");

		Login mpLogin = Factory.create(Login.class);
		LoginData mpLoginData = Factory.create(LoginData.class);
		mpLoginData.setOrderByColumns(LoginData.USERID_NAME);
		try {

			deletefile(OracleDirectory + "/" + vsTableName + ".sql");
			deletefile(SqlDirectory + "/" + vsTableName + ".tsql");
			writeTextFile(OracleDirectory, vsTableName + ".sql", OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;
			vpMapList = mpLogin.getAllElements(mpLoginData);
			for (Map vpMap : vpMapList) {
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpLoginData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "('"+ mpLoginData.getUserID().trim() + "',"
				        + "'" + mpLoginData.getRole().trim()	+ "', "
				         + "'" + mpLoginData.getLoginTime()	+ "', " 
						+ "'" + mpLoginData.getMachineName().trim() + "',"
				        + "'" + mpLoginData.getIPAddress() + "');"
				      	+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		} catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void Move(String OracleDirectory, String SqlDirectory) {
		String vsTableName = "Move";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ "( IMOVEID, SPARENTLOAD, SLOADID, SITEM, SPICKLOT,"
				+ " SORDERLOT, FPICKQUANTITY, IPRIORITY,"
				+ " SORDERID, SROUTEID, IMOVECATEGORY, IMOVETYPE,"
				+ " IMOVESTATUS, DMOVEDATE, SPICKTOLOADID, SLINEID,"
				+ " SPOSITIONID, SDEVICEID, SDESTWAREHOUSE,"
				+ " SDESTADDRESS, SNEXTWAREHOUSE, SNEXTADDRESS )"
				+ "VALUES "
				+ System.getProperty("line.separator");

		Move mpMove = Factory.create(Move.class);
		MoveData mpMoveData = Factory.create(MoveData.class);
		mpMoveData.setOrderByColumns(MoveData.ORDERID_NAME);
		try {

			deletefile(OracleDirectory + "/" + vsTableName + ".sql");
			deletefile(SqlDirectory + "/" + vsTableName + ".tsql");
			writeTextFile(OracleDirectory, vsTableName + ".sql", OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;
			vpMapList = mpMove.getAllElements(mpMoveData);
			for (Map vpMap : vpMapList) {
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpMoveData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "("+ mpMoveData.getMoveID() + ","
				        + "'" + mpMoveData.getParentLoad().trim()	+ "', "
						+ "'" + mpMoveData.getLoadID().trim() + "',"
				        + "'" + mpMoveData.getItem().trim() + "',"
				        + "'" + mpMoveData.getPickLot().trim() + "',"
				        + "'" + mpMoveData.getOrderLot().trim() + "',"
						+ mpMoveData.getPickQuantity() + ", "
						+ mpMoveData.getPriority()+ ","
						+ "'" + mpMoveData.getOrderID().trim()+ "',"
						+ "'" + mpMoveData.getRouteID().trim() + "',"
						+ mpMoveData.getMoveCategory() + ","
						+ mpMoveData.getMoveType() + ","
						+ mpMoveData.getMoveStatus() + ","
						+ "'" + mpMoveData.getMoveDate()+ "',"
						+ "'" + mpMoveData.getPickToLoad().trim()+ "',"
						+ "'" + mpMoveData.getLineID().trim() + "',"
						+ "'" + mpMoveData.getPositionID().trim() + "',"
						+ "'" + mpMoveData.getDeviceID().trim() +"',"
						+ "'" + mpMoveData.getDestWarehouse().trim() +"',"
						+ "'" + mpMoveData.getDestAddress().trim() +"',"
						+ "'" + mpMoveData.getNextWarehouse().trim() +"',"
						+ "'" + mpMoveData.getNextWarehouse().trim() +"');"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		} catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void OrderHeader(String OracleDirectory, String SqlDirectory)
 {
		String vsTableName = "OrderHeader";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ "( SORDERID, DORDEREDTIME, DSCHEDULEDDATE, "
				+ "DSHORTORDERCHECKTIME, IPRIORITY,"
				+ " IORDERTYPE, SDESTINATIONSTATION, "
				+ "SDESCRIPTION, IORDERSTATUS, INEXTSTATUS, "
				+ "SORDERMESSAGE, SRELEASETOCODE, SCARRIERID,"
				+ " SDESTWAREHOUSE, SDESTADDRESS, SSHIPCUSTOMER,"
				+ " IHOSTLINECOUNT )" + "VALUES "
				+ System.getProperty("line.separator");

		OrderHeader mpOrderHeader = Factory.create(OrderHeader.class);
		OrderHeaderData mpOrderHeaderData = Factory
				.create(OrderHeaderData.class);
		mpOrderHeaderData.setOrderByColumns(OrderHeaderData.ORDERID_NAME);
		try {

			deletefile(OracleDirectory + "/" + vsTableName + ".sql");
			deletefile(SqlDirectory + "/" + vsTableName + ".tsql");
			writeTextFile(OracleDirectory, vsTableName + ".sql", OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;
			vpMapList = mpOrderHeader.getAllElements(mpOrderHeaderData);
			for (Map vpMap : vpMapList) {
				
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpOrderHeaderData.dataToSKDCData(vpMap);
				String vsCustomer;
				if (SKDCUtility.isFilledIn(mpOrderHeaderData.getShipCustomer()))
				{
					vsCustomer ="'"+mpOrderHeaderData.getShipCustomer().trim()+ "'";	
				}
				else
				{	
					 vsCustomer = null;	
				}
				String importText = "";
				importText = "('" + mpOrderHeaderData.getOrderID().trim() + "', "
				        + "'" + mpOrderHeaderData.getOrderedTime()	+ "', "
						+ "'" + mpOrderHeaderData.getScheduledDate() + "',"
				        + "'" + mpOrderHeaderData.getShortOrderCheckTime() + "',"
						+ mpOrderHeaderData.getPriority() + ", "
						+ mpOrderHeaderData.getOrderType() + ","
						+ "'" + mpOrderHeaderData.getDestinationStation().trim()+ "',"
						+ "'" + mpOrderHeaderData.getDescription().trim() + "',"
						+ mpOrderHeaderData.getOrderStatus() + ","
						+ mpOrderHeaderData.getNextStatus() + ","
						+ "'" + mpOrderHeaderData.getOrderMessage().trim() + "',"
						+ "'" + mpOrderHeaderData.getReleaseToCode().trim() + "',"
						+ "'" + mpOrderHeaderData.getCarrierID().trim() + "',"
						+ "'" + mpOrderHeaderData.getDestWarehouse().trim() + "',"
						+ "'" + mpOrderHeaderData.getDestAddress().trim() + "',"
						+ vsCustomer + ","
						+ mpOrderHeaderData.getHostLineCount() + ");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		} catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void OrderLine(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "OrderLine";
		String vpTableHeader = "	INSERT INTO asrs." + vsTableName
				+ "(SORDERID, SITEM, SORDERLOT, SLINEID, SROUTEID, "
				+ " FORDERQUANTITY, FALLOCATEDQUANTITY,"
				+ " FPICKQUANTITY, FSHIPQUANTITY, SLOADID,"
				+ " SDESCRIPTION, SCONTAINERTYPE, SWAREHOUSE,"
				+ " SBEGINWAREHOUSE, SBEGINADDRESS, SENDINGWAREHOUSE,"
				+ " SENDINGADDRESS, IHEIGHT, ILINESHY )" + "VALUES "
				+ System.getProperty("line.separator");

		OrderLine mpOrderLine = Factory.create(OrderLine.class);
		OrderLineData mpOrderLineData = Factory.create(OrderLineData.class);
		mpOrderLineData.setOrderByColumns(OrderLineData.ORDERID_NAME);
		try {

			deletefile(OracleDirectory + "/" + vsTableName + ".sql");
			deletefile(SqlDirectory + "/" + vsTableName + ".tsql");
			writeTextFile(OracleDirectory, vsTableName + ".sql", OracleHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName + ".tsql", "delete asrs."
					+ vsTableName + ";" + System.getProperty("line.separator"));

			List<Map> vpMapList;
			vpMapList = mpOrderLine.getAllElements(mpOrderLineData);
			for (Map vpMap : vpMapList) {
				writeTextFile(SqlDirectory, vsTableName + ".tsql", vpTableHeader);
				mpOrderLineData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "('" + mpOrderLineData.getOrderID().trim() + "', "
				        + "'" + mpOrderLineData.getItem().trim()	+ "', "
						+ "'" + mpOrderLineData.getOrderLot().trim() + "',"
				        + "'" + mpOrderLineData.getLineID().trim() + "',"
				        + "'" + mpOrderLineData.getRouteID().trim() + "',"
						+ mpOrderLineData.getOrderQuantity() + ", "
						+ mpOrderLineData.getAllocatedQuantity() + ","
						+ mpOrderLineData.getPickQuantity() + ","
						+ mpOrderLineData.getShipQuantity() + ","
						+ "'" + mpOrderLineData.getLoadID().trim()+ "',"
						+ "'" + mpOrderLineData.getDescription().trim() + "',"
						+ "'" + mpOrderLineData.getContainerType().trim() + "',"
						+ "'" + mpOrderLineData.getWarehouse().trim() + "',"
						+ "'" + mpOrderLineData.getBeginWarehouse().trim() + "',"
						+ "'" + mpOrderLineData.getBeginAddress().trim() + "',"
						+ "'" + mpOrderLineData.getEndingWarehouse().trim() + "',"
						+ "'" + mpOrderLineData.getEndingAddress().trim() + "',"
						+ mpOrderLineData.getHeight() + ", "		
						+ mpOrderLineData.getLineShy() +");"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory, vsTableName + ".tsql", importText);
			}
			writeTextFile(SqlDirectory, vsTableName + ".tsql", SQLFooter);
		} catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void Port(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "Port";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
	                                                + "( SPORTNAME,SDEVICEID, IDIRECTION, "
	                                                + "ILASTSEQUENCE, ICOMMUNICATIONMODE, "
	                                                + "SSERVERNAME, SSOCKETNUMBER, "
	                                                + "IRETRYINTERVAL,ISNDKEEPALIVEINTERVAL, "
	                                                + "IRCVKEEPALIVEINTERVAL )"
	                                                + "VALUES "
			                                    	+ System.getProperty("line.separator");
		
		Port mpPort = Factory.create(Port.class);
		PortData mpPortData = Factory.create(PortData.class);
		mpPortData.setOrderByColumns(PortData.PORTNAME_NAME);
		try {

			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			vpMapList = mpPort.getAllElements(mpPortData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpPortData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "('" + mpPortData.getPortName().trim() + "', "
						+ "'" + mpPortData.getDeviceID().trim() + "', "
						+ mpPortData.getDirection() + ","
						+ mpPortData.getLastSequence() + ","
						+ mpPortData.getCommunicationMode() + ", " 
						+ "'"+ mpPortData.getServerName().trim() + "',"
						+ "'"+ mpPortData.getSocketNumber().trim() + "',"
						+ mpPortData.getRetryInterval() + ","
						+ mpPortData.getSndKeepAliveInterval() + ","
						+ mpPortData.getRcvKeepAliveInterval() + ")"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}
		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void PurchaseOrderHeader(String OracleDirectory, String SqlDirectory) {
	}

	public void PurchaseOrderLine(String OracleDirectory, String SqlDirectory) {
	}

	public void Role(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "Role";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ " (SROLE, SROLEDESCRIPTION, IROLETYPE)"
				+ "VALUES "
				+ System.getProperty("line.separator");
		
		Role mpRole = Factory.create(Role.class);
		RoleData mpRoleData = Factory.create(RoleData.class);
		mpRoleData.setOrderByColumns(RoleData.ROLE_NAME);
		try {

			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			RoleData vpRoleData = Factory.create(RoleData.class);
			vpMapList = mpRole.getAllElements(vpRoleData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpRoleData.dataToSKDCData(vpMap);
				String importText = "";
				importText = "('" + mpRoleData.getRole().trim() + "', " 
						+  "'" + mpRoleData.getRoleDescription().trim() + "', "
						+   mpRoleData.getRoleType() + " );"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		} catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void RoleOption(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "RoleOption";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ " (SROLE, SCATEGORY, SOPTION, SICONNAME, SCLASSNAME, " 
                + "IBUTTONBAR, IADDALLOWED, IMODIFYALLOWED, IDELETEALLOWED, "
				+ "IVIEWALLOWED)"
				+ "VALUES "
				+ System.getProperty("line.separator");
		
		RoleOption mpRoleOption = Factory.create(com.daifukuamerica.wrxj.dbadapter.data.RoleOption.class);
		RoleOptionData mpRoleOptionData = Factory.create(RoleOptionData.class);
		mpRoleOptionData.setOrderByColumns(RoleOptionData.ROLE_NAME);
		try {

			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			vpMapList = mpRoleOption.getAllElements(mpRoleOptionData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpRoleOptionData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpRoleOptionData.getRole().trim() + "', "
					     + "'"+ mpRoleOptionData.getCategory().trim() + "'," 
						 +"'" + mpRoleOptionData.getOption().trim() + "', "
						 + "'" +mpRoleOptionData.getIconName().trim() + "'," 
						 + "'" +mpRoleOptionData.getClassName().trim() + "'," 
						 + mpRoleOptionData.getButtonBar() + ","
						 +mpRoleOptionData.getAddAllowed()+ ","
						 +mpRoleOptionData.getModifyAllowed()+ ","
						 +mpRoleOptionData.getDeleteAllowed()+ ","
						 +mpRoleOptionData.getViewAllowed()+ ")"
						 + System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	public void Route(String OracleDirectory, String SqlDirectory)
	{
		String vsTableName = "Route";
		String vpTableHeader = "	INSERT INTO asrs."+ vsTableName
				+ " (SROUTEID, SFROMID, SDESTID, IFROMTYPE, IDESTTYPE, "
                + "  IROUTEONOFF)"
				+ "values"
				+ System.getProperty("line.separator");
		
		Route mpRoute = Factory.create(Route.class);
		RouteData mpRouteData = Factory.create(RouteData.class);
		mpRouteData.setOrderByColumns(mpRouteData.ROUTEID_NAME);
		try {

			deletefile(OracleDirectory +"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory +"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			vpMapList = mpRoute.getAllElements(mpRouteData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpRouteData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpRouteData.getRouteID().trim() + "', "
					     + "'"+ mpRouteData.getFromID().trim() + "'," 
						 +"'" + mpRouteData.getDestID().trim() + "', "
						 + mpRouteData.getFromType() + ","
						 +mpRouteData.getDestType()+ ","
						 +mpRouteData.getRouteOnOff() +  ")"
							 + System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}
	
	public void Station(String OracleDirectory, String SqlDirectory)
    {
	   String vsTableName = "Station";
	   String vpTableHeader = "	INSERT INTO asrs."+ vsTableName	
				+ " (SSTATIONNAME, SWAREHOUSE, SDESCRIPTION,"
				+ " ISTATIONTYPE, SALLOCATIONTYPE, IDELETEINVENTORY,"
				+ " SDEFAULTROUTE, SDEVICEID, IARRIVALREQUIRED,"
				+ " IMAXALLOWEDENROUTE, IMAXALLOWEDSTAGED,"
				+ " IAUTOLOADMOVEMENTTYPE, IAUTOORDERTYPE,"
				+  "IALLOCATIONENABLED, ISTATUS, IBIDIRECTIONALSTATUS,"
				+ " ICAPTIVE, SCONTAINERTYPE, IPHYSICALSTATUS, IPORECEIVEALL,"
				+ " ICCIALLOWED, FWEIGHT, IHEIGHT, IAMOUNTFULL, FORDERQUANTITY,"
				+ " IALLOWROUNDROBIN, ICUSTOMACTION,"
				+ " ICONFIRMQTY, ICONFIRMLOT, ICONFIRMLOCATION, ICONFIRMLOAD,"
				+ " ICONFIRMITEM, IORDERSTATUS)" 
				+ " values "
				+ System.getProperty("line.separator");
	
	Station mpStation = Factory.create(Station.class);
	StationData mpStationData = Factory.create(StationData.class);
	mpStationData.setOrderByColumns(StationData.STATIONNAME_NAME);
	try 
	{
		deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
		deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
		writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
		writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
		writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
		
		List<Map> vpMapList;
		StationData vpStationData = Factory.create(StationData.class);
		vpMapList = mpStation.getAllElements(vpStationData);
		for (Map vpMap : vpMapList)
		{
			writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
			vpStationData.dataToSKDCData(vpMap);
			String importText = "";

				importText = "('" + vpStationData.getStationName().trim()
						+ "', '" + vpStationData.getWarehouse().trim()+ "'," 
						+"'" + vpStationData.getDescription().trim() + "',"
						+  vpStationData.getStationType() + ","
						+ "'" + vpStationData.getAllocationType().trim() + "', "
						+ vpStationData.getDeleteInventory() + ","
						+ "'" + vpStationData.getDefaultRoute().trim() + "', "
						+ "'" + vpStationData.getDeviceID().trim() + "', "
						+ vpStationData.getArrivalRequired() + ","
						+ vpStationData.getMaxAllowedEnroute() + ","
						+ vpStationData.getMaxAllowedStaged() + ", "
						+ vpStationData.getAutoLoadMovementType() + ","
						+ vpStationData.getAutoOrderType() + ", "
						+ vpStationData.getAllocationEnabled() + ", "
						+ vpStationData.getStatus() + ", "
						+ vpStationData.getBidirectionalStatus() + ","
						+ vpStationData.getCaptive() +  ","
						+"'" + vpStationData.getContainerType().trim() + "', "
						+ vpStationData.getPhysicalStatus() + ","
						+ vpStationData.getPoReceiveAll() + ","
						+ vpStationData.getCCIAllowed() + ", "
						+ vpStationData.getWeight() + ", "
						+ vpStationData.getHeight() + ","
						+ vpStationData.getAmountFull() + ","
						+ vpStationData.getOrderQuantity() + ","
						+ vpStationData.getAllowRoundRobin() + ","
						+ vpStationData.getCustomAction() + ","
						+ vpStationData.getConfirmLot() + ", "
						+ vpStationData.getConfirmLot() + ", "
						+ vpStationData.getConfirmLocation() + ","
						+ vpStationData.getConfirmLoad() + ","
						+ vpStationData.getConfirmItem() + ", "
						+ vpStationData.getOrderStatus() + ")"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
	}

	catch (DBException e) {
		throw new RuntimeException("IO Error occured");
	}
	}

	public void Synonyms(String OracleDirectory, String SqlDirectory) {
	}

	public void SysConfig(String OracleDirectory, String SqlDirectory)
	{
		   String vsTableName = "SysConfig";
		   String vpTableHeader = "	INSERT INTO asrs."+ vsTableName	
					+ " (SGROUP, SPARAMETERNAME, SPARAMETERVALUE,"
					+ " ISCREENCHANGEALLOWED, IENABLED)"
					+ " values "
					+ System.getProperty("line.separator");
		
		SysConfig mpSysconfig = Factory.create(SysConfig.class);
		SysConfigData mpSysConfigData = Factory.create(SysConfigData.class);
		mpSysConfigData.setOrderByColumns(SysConfigData.GROUP_NAME);
		try 
		{
			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/"+ vsTableName+ ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
		
			vpMapList = mpSysconfig.getAllElements(mpSysConfigData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpSysConfigData.dataToSKDCData(vpMap);
				String importText = "";

					importText = 
							"('" + mpSysConfigData.getGroup().trim() + "'," 
					               + "'" + mpSysConfigData.getParameterName().trim()+ "'," 
						           +"'" + mpSysConfigData.getParameterValue().trim() + "',"
					        	   + mpSysConfigData.getScreenChangeAllowed() + ","
							       + mpSysConfigData.getScreenChangeAllowed() + ")"
							       + System.getProperty("line.separator");
					writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
				}
				writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	public void TransActionHistory(String OracleDirectory, String SqlDirectory)
	{
	
    }

	private void VehicleMove(String OracleDirectory, String SqlDirectory) {
	}

	private void VehicleSystemCMD(String OracleDirectory, String SqlDirectory) {
	}

	private void Warehouse(String OracleDirectory, String SqlDirectory)
	{
		   String vsTableName = "Warehouse";
		   String vpTableHeader = "	INSERT INTO asrs."+ vsTableName	
					+ " (SSUPERWAREHOUSE, SWAREHOUSE, SDESCRIPTION,"
				+ " IWAREHOUSETYPE, IWAREHOUSESTATUS, IONELOADPERLOC,"
				+ " SEQUIPWAREHOUSE)"
				+ " values " + System.getProperty("line.separator");
		   
		Warehouse mpWarehouse = Factory.create(Warehouse.class);
		
		WarehouseData mpWarhouseData = Factory.create(WarehouseData.class);
		mpWarhouseData.setOrderByColumns(StationData.WAREHOUSE_NAME);
		try
		{
			deletefile(OracleDirectory+"/"+ vsTableName+ ".sql");
			deletefile(SqlDirectory+"/" + vsTableName + ".tsql");
			writeTextFile(OracleDirectory, vsTableName+".sql",OracleHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", SQLHeader);
			writeTextFile(SqlDirectory, vsTableName+".tsql", 	"delete asrs." + vsTableName+ ";" + System.getProperty("line.separator"));
			
			List<Map> vpMapList;
			vpMapList = mpWarehouse.getAllElements(mpWarhouseData);
			for (Map vpMap : vpMapList)
			{
				writeTextFile(SqlDirectory, vsTableName+ ".tsql",vpTableHeader);
				mpWarhouseData.dataToSKDCData(vpMap);
				String importText = "";

				importText = "('" + mpWarhouseData.getSuperWarehouse().trim() + "',"
						+ "'" + mpWarhouseData.getWarehouse().trim()+ "',"
						+ "'" + mpWarhouseData.getDescription().trim()+ "',"
						+ mpWarhouseData.getWarehouseType() + ","
						+ mpWarhouseData.getWarehouseStatus() + ", "
						+ mpWarhouseData.getOneLoadPerLoc() + ","
						+ "'" + mpWarhouseData.getEquipWarehouse().trim() + "');"
						+ System.getProperty("line.separator");
				writeTextFile(SqlDirectory,vsTableName+ ".tsql", importText);
			}
			writeTextFile(SqlDirectory,vsTableName+ ".tsql",SQLFooter);
		}

		catch (DBException e) {
			throw new RuntimeException("IO Error occured");
		}
	}

	private void WrxSequence(String OracleDirectory, String SqlDirectory) {
	}

	private void Zone(String OracleDirectory, String SqlDirectory) {
	}

	private void ZoneGroup(String OracleDirectory, String SqlDirectory) {
	}

	protected static void deletefile(String file)
	{
	        (new File(file)).delete();
	}

	/*
* 
*/
	public static void writeTextFile(String vsDirectory, String vsfileName, String s) {
	    if (!new File(vsDirectory).exists())
	    {
//	      String logDirectory = saveLogsFilePath.substring(0, saveLogsFilePath.length()-1); // lose file separator
	      if (new File(vsDirectory).mkdirs())
	      {
	       
	      }
	      else
	      {
	        System.err.println("UNABLE To Create Log Directory \"" + vsDirectory + "\"");
	      }
	    }
	    writeTextFile( vsDirectory+"/"+ vsfileName, s);
	}
	public static void writeTextFile( String fileName, String s) {
	 
		FileWriter output = null;
		try {
			output = new FileWriter(fileName, true);
			BufferedWriter writer = new BufferedWriter(output);
			writer.write(s);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}
