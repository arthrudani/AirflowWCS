/**
 *
 */
package com.daifukuamerica.wrxj.web.ui.tag;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import com.daifukuamerica.wrxj.dbadapter.AbstractSKDCData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaData;
import com.daifukuamerica.wrxj.dbadapter.data.AsrsMetaDataData;
import com.daifukuamerica.wrxj.factory.Factory;
import com.daifukuamerica.wrxj.jdbc.DBException;
import com.daifukuamerica.wrxj.jdbc.DBObject;
import com.daifukuamerica.wrxj.jdbc.DBObjectTL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: dystout
 * Created : Apr 5, 2017
 *
 * Convenience Tag to create an ajax table from asrs metadata for view.
 *
* The table's ID is used to identify it from duplicate instance of DataTables
	 * it is also provides the following instances of DataTable javascript/css variables:
	 *
	 *<br><br><b>JavaScript Variables:</b>
	 * 	<ul><li>table(tableId) - unique instance of DataTables table</li>
	 * 		<li>refreshEnabled(tableId) - is the Table's auto-refresh toggled (ajax request at timed interval)</li></ul>
	 * 		<b>CSS Variables:</b>
	 * 			#toolbar-buttons.col-md-8.tblId(tableId) - toolbar div css
	 *
	 * HINT: Believe me, this looks messy... while setting a breakpoint might help it
	 * is often more useful to view the generated source in the view and use browser
	 * inspection/debug tools.
	 *
	 *  @see <a href="https://datatables.net/reference/api/">DataTables API</a> for reference
	 */
public class AjaxTableTag extends SimpleTagSupport
{

	  private AsrsMetaDataData mddata = Factory.create(AsrsMetaDataData.class);
	  private AsrsMetaData amd = Factory.create(AsrsMetaData.class);

	  private List<Map> metadata;
	  private String metaDataName;
	  private AbstractSKDCData skdcData ;
	  protected String genJavaScript;
	  protected String genHtml;
	  protected String tableId = "";
	  protected boolean hasSearch = false;
	  protected boolean hasAdd = false;
	  protected boolean hasRefresh = false;
	  protected boolean hasExcel = false;
	  protected boolean hasColVis = false;
	  protected boolean hasAutoRefresh = false;
	  protected boolean hasFilter = false;
	  protected boolean hasOnAll = false;
	  protected boolean hasEdit = false;
	  protected boolean hasDelete = false;
	  protected boolean inModal = false;
	  protected boolean inSidebar = false;
	  protected boolean compact = false;
	  protected String theme = "";
	  protected boolean hasFooter = true;
	  private String noTranslation = "Undefined";
	  private String[] columns;
	  private String[] hideColumns;   // Explicitly define a string array of header column names to be hidden from the table
	  private String prefHideColumns; // Apply user-preference column rendering, Explicitly defined hidden columns take precedent
	  private String[] translatedColumns;
	  private String ajaxUri;
	  private String[] regexHighlightList; //each string in the array is a comma delimited entry specifying
	  									   //the following index values 0 - column index evaluated by regular expression, 1-regular expression,
	  									   // 2-where to apply color ('color','background-color'), 3-what color to apply

	  private String metaId;
	  private DBObject dbobj;
	  private Integer numPageLength = 15;
	  private Integer refreshRateSec = 5;

	  /**
	* Log4j logger: AjaxTableTag
	*/
	private static final Logger logger = LoggerFactory.getLogger(AjaxTableTag.class);



	  public AjaxTableTag()
	  {
		  //
	  }

	  /**
	 * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
	 *
	 * Generate Table
	 *
	 */
	@Override
	public void doTag() throws JspException, IOException
	{
		logger.debug("Generating {} ajax table from ASRS Metadata", getMetaDataName());

		dbobj = new DBObjectTL().getDBObject();
	    if (!dbobj.checkConnected())
	    {
	      try { dbobj.connect(); } catch(DBException e) { logger.error(e.getMessage()); }
	    }
		try
		{
			mddata.clear();
			columns = amd.getOrderedColumns(getMetaDataName());
			translatedColumns = columns.clone();
			for(int i=0; i<=columns.length-1;i++)
			{
				translatedColumns[i] = amd.getFullName(getMetaDataName(), columns[i]);
			}
			mddata.setKey(AsrsMetaDataData.DATAVIEWNAME_NAME, getMetaDataName());
			metadata = amd.getAllElements(mddata);
		} catch (DBException e)
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		JspWriter out = getJspContext().getOut();
		out.println(generateTable() + generateJs());



	}




	public String generateTable()
	{
		StringBuilder sb = new StringBuilder();

		if (!inModal)//dont wrap in inner content class for modal tables
		{
			sb.append(" <div class=\"inner-content\">"); //dont w
		}

		sb.append("<div id=\"tblWrap" + tableId + "\"");
		if(!inModal){ //no panel class for modal tables
			sb.append(" class=\"theme-panel panel\"");
		}
		sb.append(">");

		if (!inModal)//no panel body for modal tables
		{
			sb.append("<div class=\"panel-body\">");
		}

		// table
		sb.append("<table id=\"ajaxTable" + tableId + "\" class=\"table table-striped table-bordered ");
		if(compact){
			sb.append(" compact ");
		}
		if(theme.equalsIgnoreCase("inverse"))
			sb.append("table-inverse");
		sb.append("\" cellspacing=\"0\" width=\"100%\">"
		// head
		+ "<thead><tr>");

		for (int i = 0; i <= translatedColumns.length - 1; i++)
		{
			sb.append("<th>" + translatedColumns[i].toString() + "</th>");
		}

		sb.append("</tr>"
				+ "</thead>"
				+ "<tbody></tbody>" );
		if(hasFooter)
		{
			sb.append("<tfoot><tr>");
			for(int i=0; i<=translatedColumns.length-1; i++)
			{
				sb.append("<th>"+translatedColumns[i].toString()+"</th>");
			}
			sb.append("</tr></tfoot>");
		}
		sb.append("</table></div>");
		if(!inSidebar)
		{
			sb.append("</div>");
		}
		if(!inModal)
		{
			sb.append("</div></div>");
		}

		return sb.toString();


	}

	/**
	 * Generate the javascript necessary for creating an instance of DataTables with
	 * some decorators passed by the <wrxj:ajaxTable> parameters.
	 *
	 *
	 */
	public String generateJs()
	{
		StringBuilder sb = new StringBuilder("<script>");
		
		if(regexHighlightList!=null&&regexHighlightList.length>0){
			for(int i=0; i<regexHighlightList.length; i++)
			{
				String[] conditions = regexHighlightList[i].split(","); //0 - column index evaluated, 1-regular expression, 2-where to apply color, 3-what color to apply
				sb.append("var re"+i+"Expr= new RegExp(\""+conditions[1]+"\");");
			}
		}
		if (hasExcel)
		{
		  sb.append("var forceText"+tableId+"Idx = [];");
		}
		sb.append( "var refreshEnabled"+tableId+"=false;"
				+ "var table"+ tableId + " = $('#ajaxTable"+ tableId + "').DataTable({" //instantiate table
				+"orderClasses: false," //sorting styling disabled
				+" deferRender: true,"
				+" stateSave: true, "
				+"\"drawCallback\": function(settings){ var api = this.api(); if(typeof on"+tableId+"TableDraw === \"function\") on"+tableId+"TableDraw(api);}," //every time the table is drawn, fire off a function named on+tableId+TableDraw() and will pass data tables api as param
				+"'stripeClasses':['stripe1','stripe2']," // stylize the tables stripes using css selectors
				+ "ajax:'" +getAjaxUri() + "'," // URL to be executed on reload to fetch TableDataModel
				+ "\"initComplete\": function(settings, json) {$(\".paginate_button > a\").on(\"focus\", function(){$(this).blur();});}," // Do not scroll to bottom on reload of ajax call
				+ "pageLength:" + getNumPageLength() +"," // how many results shown in one table page
				+ "dom: '<\"#toolbar\"<\"#toolbar-buttons.col-md-8.tblId"+ tableId + "\"B><\"#toolbar-filter.col-md-4\""); // structure the toolbar above the table with datatables calls
				//table element placing via DataTables dom
		if(hasFilter) 		// filter search box
			sb.append("f"); //add filter text box
		sb.append(">>rtip', columns: ["); //define columns
		String[] userPreferenceHideColumn = null;
		if(prefHideColumns!=null){
			 userPreferenceHideColumn = prefHideColumns.split(","); // split 0's and 1's by comma delimiter to determine which column indexs are to be hidden 1=show 0=hide
		}
		
		for(int i=0; i<=translatedColumns.length-1; i++){ 		// list out our json mappings & add default values for nulls of <i>null</i>
			
			sb.append("{ \"data\":\""+ translatedColumns[i].toString() + "\", \"defaultContent\": \"<i>-</>\", \"name\":\"" + translatedColumns[i].toString() +"\" ");
			
			boolean colvis = true;
			if(hideColumns!=null&&hideColumns.length>=1){

				if(Arrays.asList(hideColumns).contains(translatedColumns[i].toString())){ // if the translated column is verbatim with a hidden column we specified
					sb.append(",\"visible\": false"); // hide column
					colvis=false;
				}

			}
			if(userPreferenceHideColumn!=null && colvis && translatedColumns.length == userPreferenceHideColumn.length)
			{
				if(userPreferenceHideColumn[i].equalsIgnoreCase("0")){
					sb.append(",\"visible\": false"); // hide column
					colvis=false;
				}

			}
			sb.append("}");
			if(i!=translatedColumns.length-1)
				sb.append(",");
		}
		sb.append("],rowId:'" + getMetaId() +"',"); //set row ids for context menu selection
		sb.append("lengthChange: false,	select: { style: 'os' }, buttons: [ ");

		//Buttons
		if(hasAdd)
			sb.append("	{ text:'<i class=\"fa fa-plus-circle fa-2x fa-fw\" aria-hidden=\"true\"></i>'," // open generic add modal
						+ "action: function(e,dt,node,config) {	$('#add-modal').modal('show'); }},");
		if(hasRefresh)
			sb.append("{text:'<i class=\"fa fa-refresh fa-2x fa-fw\" aria-hidden=\"true\"></i>',"  // reload the table on button press
						+"action: function(e,dt,node,config) { showLoadingAnimation('"+ tableId + "'); table"+ tableId + ".ajax.reload(); hideLoadingAnimation('"+ tableId + "');}},");
		if(hasEdit)
			sb.append("{text:'<i class=\"fa fa-pencil fa-2x fa-fw\" aria-hidden=\"true\"></i>'," // open generic edit modal
						+"action: function(e,dt,node,config) { $('#edit-modal').modal('show'); }},");
		if(hasDelete)
			sb.append("{text:'<i class=\"fa fa-trash fa-2x fa-fw\" aria-hidden=\"true\"></i>'," 		// open generic delete modal
						+"action: function(e,dt,node,config) { $('#delete-modal').modal('show');}},");
		
		if(hasExcel) {
		    /*
		     * Numeric-looking columns are saved as numbers by default, and this loses leading 
		     * zeroes on columns like address.  To avoid this, add column indices of affected 
		     * columns to forceText"+tableId+"Idx.
		     */
			sb.append("{ extend: 'excel',"
			    + " text: '<i class=\"fa fa-file-excel-o fa-2x fa-fw\" aria-hidden=\"true\"></i>',"
			    + " exportOptions: { "
                + "  format: { " 
			    + "   body: function (data, row, column, node ) { "
                + "    return (forceText"+tableId+"Idx.includes(column)) ? '\\0' + data : data;" 
                + "   }"
                + "  }" 
                + " }" 
                + "},"); // export to excel data tables function
		}
		if(hasColVis)
			sb.append("{ extend: 'colvis', text: '<i class=\"fa fa-columns fa-2x fa-fw\" aria-hidden=\"true\"></i>', postfixButtons: ['colvisRestore'] },");  // hide/show existing columns data tables function
		if(hasOnAll) {
//			sb.append("refreshEnabled"+tableId+"=true; ");
//			sb.append("setInterval(function(){ if(refreshEnabled"+tableId+"){"
//					+ "showLoadingAnimation('"+ tableId + "'); "
//					//+ "table.ajax.reload(null, false); "
//					+ "table"+ tableId + ".ajax.reload(function(){$(\".paginate_button > a\").on(\"focus\", function(){$(this).blur();});}, false);"
//					+ " hideLoadingAnimation('"+ tableId + "');}},");
//			sb.append(refreshRateSec*1000 + "); "); // default is 5 seconds
//			sb.append("$(\"div.dt-buttons.btn-group \", \"#tblWrap"+tableId+"\").append('<label class=\"checkbox-inline\"> "
//					+ "<input id=\"alert_toggle"+tableId+"\" type=\"checkbox\" name=\"alert_toggle\" data-toggle=\"toggle\" data-onstyle=\"success\" data-height=\"42\" data-offstyle=\"danger\" checked> <span class=\"label label-default\">On/Off All Alerts</span></label>'); 	 "
//					+ "$(document).ready(function(){ console.log('alerts started for - "+tableId+" to state:'+ refreshEnabled"+tableId+"); $(\"#alert_toggle"+tableId+"\").change(function () { "
//					+ " refreshEnabled"+tableId+" =!refreshEnabled"+tableId+"; console.log('alerts toggled to state:'+ refreshEnabled"+tableId+");"
//					+ "}); }); ");
			
//			sb.append("<input type=\"checkbox\" class=\"multi-switch\" \r\n"
//					+ "       initial-value=\"0\" \r\n"
//					+ "       unchecked-value=\"2\" \r\n"
//					+ "       checked-value=\"1\" \r\n"
//					+ "       value=\"0\"\r\n"
//					+ ">");
//			sb.append("$('.multi-switch').multiSwitch({\r\n"
//					+ "  functionOnChange: function ($element) {\r\n"
//					+ "    // do something here\r\n"
//					+ "  }\r\n"
//					+ "});");
			sb.append("{text:'<i class=\"fa fa-toggle-on fa-2x fa-fw\" aria-hidden=\"true\"></i>'," // on off all alerts
						+"action: function(e,dt,node,config) { console.log('ON OFF alerts') }},");
		}
		if(hasSearch)
			sb.append("	{ text:'<i class=\"fa fa-search fa-2x fa-fw\" aria-hidden=\"true\"></i>',"
						+"action: function(e,dt,node,config){ $(\"#search-modal\").modal('show');	}}"); // open generic search modal (page specific)

		sb.append("], responsive: true, \"scrollX\": true, ");
		
		
		if(regexHighlightList!=null && regexHighlightList.length>=1)
		{
			sb.append("rowCallback: function(row, data, index){ ");
//			sb.append("console.log(\"Row callback - \");");
			for(int i=0; i<regexHighlightList.length; i++)
			{
				String[] conditions = regexHighlightList[i].split(","); //0 - column index evaluated, 1-regular expression, 2-css-class selector
//				sb.append("console.log(\"DATA | \" + JSON.stringify(data['"+conditions[0]+"']));");
				sb.append(" if(re"+i+"Expr.test(data['"+conditions[0]+"'])){"); //if our data in cell at specified column matches the regular expression
//				sb.append("console.log(\"Data Match! data['"+conditions[0]+"'] "+ i +"\");");
				sb.append(" $(row).addClass('"+ conditions[2] + "');}");
			}
			sb.append("},");
		}
//		if(hideColumns !=null && hideColumns.length>=1)
//		{
//			sb.append("columnDefs: [ { targets: [");
//			for(int i=0;i<hideColumns.length;i++)
//			{
//				sb.append(" '"+hideColumns[i]+"'");
//				if(i!=hideColumns.length-1)
//					sb.append(",");
//			}
//			sb.append("], visible: false }],");
//		}

		sb.append("\"oLanguage\": {\"sSearch\": \"Filter:\","
				+ "\"emptyTable\":\"No Results\"} });");

		if(hasAutoRefresh){ // add ability to refresh at a given interval, include toggle switch to toggle functionality of refresh
			sb.append("refreshEnabled"+tableId+"=true; ");
			sb.append("setInterval(function(){ if(refreshEnabled"+tableId+"){"
					+ "showLoadingAnimation('"+ tableId + "'); "
					//+ "table.ajax.reload(null, false); "
					+ "table"+ tableId + ".ajax.reload(function(){$(\".paginate_button > a\").on(\"focus\", function(){$(this).blur();});}, false);"
					+ " hideLoadingAnimation('"+ tableId + "');}},");
			sb.append(refreshRateSec*1000 + "); "); // default is 5 seconds

			sb.append("$(\"div.dt-buttons.btn-group \", \"#tblWrap"+tableId+"\").append('<label class=\"checkbox-inline\"> "
					+ "<input id=\"auto_refresh_toggle"+tableId+"\" type=\"checkbox\" name=\"auto_refresh_toggle\" data-toggle=\"toggle\" data-onstyle=\"success\" data-height=\"42\" data-offstyle=\"danger\" checked> <span class=\"label label-default\">Auto Refresh</span></label>'); 	 "
					+ "$(document).ready(function(){ console.log('autorefresh started for - "+tableId+" to state:'+ refreshEnabled"+tableId+"); $(\"#auto_refresh_toggle"+tableId+"\").change(function () { "
					+ " refreshEnabled"+tableId+" =!refreshEnabled"+tableId+"; console.log('autorefresh toggled to state:'+ refreshEnabled"+tableId+");"
					+ "}); }); ");


		}
		
			
		
		//LOADING GIF HOLDER
		sb.append("$(\"div.dt-buttons.btn-group \", \"#tblWrap"+tableId+"\").append('<span id=\"loading_gif_container"+ tableId + "\"></span>');");
		//sb.append(" table"+tableId+".columns.adjust().draw(false);");
		
		/** Column Visibility listener to  for when a column has been hidden/shown */

		
		if(hasColVis)
			sb.append("$('#ajaxTable"+tableId+"').on('responsive-resize.dt', function(e, datatable, columns){ updateHiddenColumns('"+getMetaDataName()+"', columns); console.log(  columns ); });");


//		//ON PAGE LOAD RECALCULATE AND REDRAW THE COLUMNS SO THAT WE DON'T GET WONKY FORMATTING ON FIRST LOAD (timeout of 650ms for all elements to load)
		sb.append("$(document).ready(function(){ table" +tableId+".columns.adjust().draw();}); " );

		/**
		 * Table selection retention
		 * When a row in the table is selected or deselected loop through selected elements
		 * and add to a javascript array titled tableId+'selectedRows'
		 * Also logs to console for selection visualisation TODO remove log for non-debug mode
		 **/
		sb.append("var "+tableId+"selectedRows = [];");
		sb.append("if(typeof table"+tableId+" !== 'undefined'){ "+
		"if(table"+tableId+"!=null){"+
			"table"+tableId+".on('select.dt', function() { "+
				  tableId+"selectedRows = []; "+
				  "table"+tableId+".rows('.selected').every(function(rowIdx) { "+
				  	tableId+"selectedRows.push(table"+tableId+".row(rowIdx).id())"+ //add to array
				  "});"+
				  "console.log("+tableId+"selectedRows);"+
				"});"+
			"table"+tableId+".on('deselect.dt', function() { "+
				 tableId+"selectedRows = []; "+
				"table"+tableId+".rows('.selected').every(function(rowIdx){"+
					tableId+"selectedRows.push(table"+tableId+".row(rowIdx).id())"+ //add to array
				"});"+
				"console.log("+tableId+"selectedRows);"+
			"});"+
		"}"+
		"}");
		
		
		
		sb.append("var temp = "+tableId+"selectedRows;");
		sb.append("</script>");
		
		

		return sb.toString();

	}

	public String getMetaDataName()
	{
		return metaDataName;
	}

	public String getGenJavaScript()
	{
		return genJavaScript;
	}

	public void setGenJavaScript(String genJavaScript)
	{
		this.genJavaScript = genJavaScript;
	}

	public String getGenHtml()
	{
		return genHtml;
	}

	public void setGenHtml(String genHtml)
	{
		this.genHtml = genHtml;
	}

	public void setMddata(AsrsMetaDataData mddata)
	{
		this.mddata = mddata;
	}

	public void setAmd(AsrsMetaData amd)
	{
		this.amd = amd;
	}

	public void setSkdcData(AbstractSKDCData skdcData)
	{
		this.skdcData = skdcData;
	}

	public void setHasSearch(boolean hasSearch)
	{
		this.hasSearch = hasSearch;
	}

	public void setHasAdd(boolean hasAdd)
	{
		this.hasAdd = hasAdd;
	}

	public void setHasRefresh(boolean hasRefresh)
	{
		this.hasRefresh = hasRefresh;
	}

	public void setHasExcel(boolean hasExcel)
	{
		this.hasExcel = hasExcel;
	}

	public void setHasColVis(boolean hasColVis)
	{
		this.hasColVis = hasColVis;
	}

	public void setHasAutoRefresh(boolean hasAutoRefresh)
	{
		this.hasAutoRefresh = hasAutoRefresh;
	}

	public void setHasFilter(boolean hasFilter)
	{
		this.hasFilter = hasFilter;
	}
	public void setHasOnAll(boolean hasOnAll)
	{
		this.hasOnAll = hasOnAll;
	}

	public void setNoTranslation(String noTranslation)
	{
		this.noTranslation = noTranslation;
	}

	public String[] getColumns()
	{
		return columns;
	}

	public void setColumns(String[] columns)
	{
		this.columns = columns;
	}

	public String getAjaxUri()
	{
		return ajaxUri;
	}

	public void setAjaxUri(String ajaxUri)
	{
		this.ajaxUri = ajaxUri;
	}

	public AsrsMetaDataData getMddata()
	{
		return mddata;
	}

	public AsrsMetaData getAmd()
	{
		return amd;
	}

	public AbstractSKDCData getSkdcData()
	{
		return skdcData;
	}

	public boolean isHasSearch()
	{
		return hasSearch;
	}

	public boolean isHasAdd()
	{
		return hasAdd;
	}

	public boolean isHasRefresh()
	{
		return hasRefresh;
	}

	public boolean isHasExcel()
	{
		return hasExcel;
	}

	public boolean isHasColVis()
	{
		return hasColVis;
	}

	public boolean isHasAutoRefresh()
	{
		return hasAutoRefresh;
	}

	public boolean isHasFilter()
	{
		return hasFilter;
	}
	public boolean isHasOnAll()
	{
		return hasOnAll;
	}

	public String getNoTranslation()
	{
		return noTranslation;
	}

	public List<Map> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(List<Map> metadata)
	{
		this.metadata = metadata;
	}

	public void setMetaDataName(String metaDataName)
	{
		this.metaDataName = metaDataName;
	}

	public String getMetaId()
	{
		return metaId;
	}

	public void setMetaId(String metaId)
	{
		this.metaId = metaId;
	}

	public Integer getNumPageLength()
	{
		return numPageLength;
	}

	public void setNumPageLength(Integer numPageLength)
	{
		this.numPageLength = numPageLength;
	}




	public String getTableId()
	{
		return tableId;
	}

	public void setTableId(String tableId)
	{
		this.tableId = tableId;
	}

	public boolean isHasEdit()
	{
		return hasEdit;
	}

	public void setHasEdit(boolean hasEdit)
	{
		this.hasEdit = hasEdit;
	}

	public boolean isHasDelete()
	{
		return hasDelete;
	}

	public void setHasDelete(boolean hasDelete)
	{
		this.hasDelete = hasDelete;
	}

	public Integer getRefreshRateSec()
	{
		return refreshRateSec;
	}

	public void setRefreshRateSec(Integer refreshRateSec)
	{
		this.refreshRateSec = refreshRateSec;
	}


	public boolean isHasFooter() {
		return hasFooter;
	}

	public void setHasFooter(boolean hasFooter) {
		this.hasFooter = hasFooter;
	}

	public boolean isInModal()
	{
		return inModal;
	}

	public void setInModal(boolean inModal)
	{
		this.inModal = inModal;
	}

	public String getTheme()
	{
		return theme;
	}

	public void setTheme(String theme)
	{
		this.theme = theme;
	}

	public String[] getHideColumns()
	{
		return hideColumns;
	}

	public void setHideColumns(String[] hideColumns)
	{
		this.hideColumns = hideColumns;
	}

	public String getPrefHideColumns()
	{
		return prefHideColumns;
	}

	public void setPrefHideColumns(String prefHideColumns)
	{
		this.prefHideColumns = prefHideColumns;
	}

	public String[] getTranslatedColumns()
	{
		return translatedColumns;
	}

	public void setTranslatedColumns(String[] translatedColumns)
	{
		this.translatedColumns = translatedColumns;
	}

	public String[] getRegexHighlightList()
	{
		return regexHighlightList;
	}

	public void setRegexHighlightList(String[] regexHighlightList)
	{
		this.regexHighlightList = regexHighlightList;
	}

	public boolean isCompact() {
		return compact;
	}

	public void setCompact(boolean compact) {
		this.compact = compact;
	}

	public boolean isInSidebar() {
		return inSidebar;
	}

	public void setInSidebar(boolean inSidebar) {
		this.inSidebar = inSidebar;
	}




}
