<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="Airflow WCS - Flight Details">
<title>FLIGHT DETAILS | Airflow WCS</title>

<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

</head>

<%@include file="../_template/navBodyWrapper.jspf" %>

<div id="divExecuting" style="margin: 0px; padding: 0px; position: fixed; right: 0px;
    top: 0px; width: 100%; height: 100%; background-color: #666666; z-index: 30001;
    opacity: .8; filter: alpha(opacity=70);display:none" >
    <p style="position: absolute; top: 30%; left: 45%; color: White;">
        Executing...<img src="<spring:url value="/resources/img/ajax-loading.gif"/>">
    </p>
</div>

<div class="container-fluid ">
		<div class="row">
			<spring:url value="/load/listSearchTest" var="formUrl"/>
			<form:form cssClass="form-horizontal" id="load-filtering-form" action="${formUrl}" method="POST" modelAttribute="loadDataModel">
				<div class="panel panel-default">
					<div class="panel-heading">
				        <h4 class="panel-title">
				          <a data-toggle="collapse" href="#collapseCriteria">Search Criteria</a>
				        </h4>
						<div class="clearfix"></div>							      
					</div>
					<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
					<div class="row">
						<div class="col-sm-5" >
						<!-------------------------------------------------------->
						<!-------First Panel: Location Info----------------------->
						<!-------------------------------------------------------->
							<div class="panel panel-default">
								<div class="panel-body">
						<!---------Location Fields------------->
									<div class="form-group-sm">
										<div class="col-sm-3 ">
										<span class="input-group-addon">Warehouse</span>
											<form:select path="warehouse" cssClass="form-control" id="scwarehouse">
												<form:option value="ALL"></form:option>
		 										<form:options items="${dropdownMenus.warehouses}" />
											</form:select>
						
										</div>
										<div class="col-sm-5">
										<span class="input-group-addon">Address</span>
											<form:input path="address" cssClass="form-control" id="address" placeholder="Address"/>
										</div>
									 </div>	
						<!---------Device ID Field------------->
									 <div class="form-group-sm">
										<div class="col-sm-3">
											<span class="input-group-addon">Device</span>
											<form:select path="deviceId" cssClass="form-control" id="scdeviceId">
												<form:option value="ALL"></form:option>
		 													<form:options items="${dropdownMenus.deviceList}" />
											</form:select>
						
										</div>
									 </div>
								</div>
							</div>
						</div>
						<!-------------------------------------------------------->
						<!-------------Second Panel: Load Info-------------------->
						<!-------------------------------------------------------->
						<div class="col-sm-5">
							<div class="panel panel-default">
								<div class="panel-body">
						<!---------Load ID Field------------->							
									<div class="form-group-sm">									
										<div class="controls col-sm-5">
											<span class="input-group-addon">Tray ID</span>
											<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Tray ID"/>
										</div>											
						<!---------Item------------->	
									<div class="controls col-sm-5">
										<span class="input-group-addon">Bag ID</span>
										<form:input path="item" cssClass="form-control" id="item" placeholder="Bag ID"/>
									</div>									
								</div>											
							</div>						
						</div>						
					</div>
						<!---------End of Second panel------------->
				
					<!-------------------------------------------------------->
					<!------------- Third Panel: Search Button---------------->
					<!-------------------------------------------------------->
					<div class="col-sm-2">
						<div class="panel noborder">
							<div class="panel-body">						
								<div class="form-group-sm">									
							<!---------Search Button------------->
									<div class="controls col-sm-5">
										<center>
											<button id="searchLoadsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
										</center>
									</div>									
								</div>											
							</div>
						
						</div>						
					</div>
					<!---------End of Third panel------------->
				</div>
<!---------End of First Row------------->	
			</div>
			</div>
		</div>							
			</form:form>
		</div>
</div>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_USER')">
<wrxj:ajaxTable metaDataName="FlightDetails" ajaxUri="/airflowwcs/flight/empty" tableId="FlightDetails" hasAutoRefresh="true"
				metaId="TrayID" hasFilter="true" hasExcel="true"  prefHideColumns="${userPref.tableColumnVisibility['Load']}" numPageLength="100"></wrxj:ajaxTable>
</security:authorize>

    
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<%@include file="../_template/alertsFloating.jspf"%>
<%@include file="flightRetrieve.jspf" %>

<script src="<spring:url value="/resources/js/flightDetails.js"/>" type="text/javascript"></script>
<security:authorize access="hasRole('ROLE_ADMIN')">
 <script type="text/javascript">
 defaultFlightId = '<%= session.getAttribute("flightId") %>'; 
 </script>
</security:authorize>
</html>