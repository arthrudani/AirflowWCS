<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8">
<title>LOAD - Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ">

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/table_scripts.jspf"%>

<div id="divExecuting" style="margin: 0px; padding: 0px; position: fixed; right: 0px;
    top: 0px; width: 100%; height: 100%; background-color: #666666; z-index: 30001;
    opacity: .8; filter: alpha(opacity=70);display:none" >
    <p style="position: absolute; top: 30%; left: 45%; color: White;">
        Executing...<img src="<spring:url value="/resources/img/ajax-loading.gif"/>">
    </p>
</div>

<div class="container-fluid">
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
							<!-------------------------------------------------------->
							<!------- Search Info ------------------------------------>
							<!-------------------------------------------------------->
							<div class="col-sm-6" >
								<div class="panel panel-default">
									<div class="panel-body">
										<!---------Load ID Field------------->
										<div class="form-group-sm">
											<div class="controls col-sm-3">
												<span class="input-group-addon">Load ID</span>
												<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Load ID"/>
											</div>
										</div>
										<!---------Location Fields------------->
										<div class="form-group-sm">
											<div class="col-sm-3 ">
												<span class="input-group-addon">Warehouse</span>
												<form:select path="warehouse" cssClass="form-control" id="scwarehouse">
													<form:option value="ALL"></form:option>
	 												<form:options items="${dropdownMenus.warehouses}" />
												</form:select>
											</div>
											<div class="col-sm-3">
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
							<!-------------Unneeded in a Load Mover------------------->
							<!-------------------------------------------------------->
							<div  style="display:none;">
								<!---------Slave Board Field------------->	
								<form:input path="mcKey" cssClass="form-control" id="mcKey" placeholder="Slave Board"/>
								<!---------Amount Full------------->	
								<form:select path="sAmountFull" cssClass="form-control" id="scAmountFull">
									<form:option value="ALL"></form:option>
									<form:options items="${dropdownMenus.amountFull}" />
								</form:select>				
								<!---------Item------------->	
								<form:input path="item" cssClass="form-control" id="item" placeholder="item ID"/>
							</div>						
							<!-------------------------------------------------------->
							<!-------------Search Button------------------------------>
							<!-------------------------------------------------------->
							<div class="col-sm-1">
								<div class="panel noborder">
									<div class="panel-body">
										<div class="form-group-sm">
											<!---------Search Button------------->
											<center>
												<button id="searchLoadsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
											</center>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</form:form>
	</div>
</div>

<security:authorize access="hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MASTER')">
	<wrxj:ajaxTable metaDataName="Load" ajaxUri="/airflowwcs/load/empty"
		metaId="Load ID" hasRefresh="true" hasFilter="true" hasAdd="true"
		hasAutoRefresh="true" hasExcel="true" hasColVis="true"
		prefHideColumns="${userPref.tableColumnVisibility['Load']}"
		hasSearch="true" numPageLength="100"></wrxj:ajaxTable>
</security:authorize>

<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<%@include file="../_template/alertsFloating.jspf"%>
<%@include file="loadMoves.jspf" %>
<%@include file="loadLocations.jspf" %>
<%@include file="loadItemDetails.jspf" %>
<%@include file="loadModify-loadmover.jspf" %>
<script src="<spring:url value="/resources/js/load-loadmover.js"/>" type="text/javascript"></script>
<!-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasRole('ROLE_ADMIN')">
	<script type="text/javascript">
		isAdmin = true;
	</script>
</security:authorize>
</html>