<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>RECOVERY - Warehouse Rx</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/executing.jspf" %>

<div class="container-fluid">
	<div class="row">
		<spring:url value="/recovery2/listSearchTest" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="load-filtering-form" action="${formUrl}" method="POST" modelAttribute="loadDataModel">
			<div class="panel panel-default">
				<%@include file="../_template/searchHeader.jspf" %>
				<div id="collapseCriteria" class="panel-collapse collapse">
					<div class="panel-body">
						<div class="row">
							<%--------------------------------------------------------%>
							<%------- First Panel: Location---------------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-4" >
								<div class="panel panel-default">
									<div class="panel-body">
										<%---------Location Fields-------------%>
										<div class="form-group-sm">
											<div class="col-sm-3 ">
												<label for="scwarehouse" class="control-label">Warehouse</label>
												<form:select path="warehouse" cssClass="form-control" id="scwarehouse">
													<form:option value="ALL"></form:option>
													<form:options items="${dropdownMenus.warehouses}" />
												</form:select>
											</div>
											<div class="col-sm-5">
												<label for="address" class="control-label">Address</label>
												<form:input path="address" cssClass="form-control" id="address" placeholder="Address"/>
											</div>
										 </div>
										<%---------Device ID Field-------------%>
										 <div class="form-group-sm">
											<div class="col-sm-4">
												<label for="scdeviceId" class="control-label">Device</label>
												<form:select path="deviceId" cssClass="form-control" id="scdeviceId">
													<form:option value="ALL"></form:option>
													<form:options items="${dropdownMenus.deviceList}" />
												</form:select>
											</div>
										</div>
									</div>
								</div>
							</div>
							<%--------------------------------------------------------%>
							<%-------------Second Panel: Load Info--------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-4">
								<div class="panel panel-default">
									<div class="panel-body">
										<%---------Load ID Field-------------%>
										<div class="form-group-sm">
											<div class="controls col-sm-4">
												<label for="loadId" class="control-label">Load ID</label>
												<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Load ID"/>
											</div>
											<%---------Slave Board Field-------------%>
											<div class="col-sm-4">
												<label for="mcKey" class="control-label">Slave Board</label>
												<form:input path="mcKey" cssClass="form-control" id="mcKey" placeholder="Slave Board"/>
											</div>
											<%---------Amount Full-------------%>
											<div class="col-sm-4">
												<label for="scAmountFull" class="control-label">Amount Full</label>
												<form:select path="sAmountFull" cssClass="form-control" id="scAmountFull">
													<form:option value="ALL"></form:option>
			 										<form:options items="${dropdownMenus.amountFull}" />
												</form:select>
											</div>
										</div>
									</div>
								</div>
							</div>
							<%--------------------------------------------------------%>
							<%-------------Third Panel: Item Info---------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-2">
								<div class="panel panel-default">
									<div class="panel-body">
										<div class="form-group-sm">
											<%---------Item-------------%>
											<div class="controls col-sm-12">
												<label for="item" class="control-label">Item</label>
												<form:input path="item" cssClass="form-control" id="item" placeholder="item ID"/>
											</div>
										</div>
									</div>
								</div>
							</div>
							<%--------------------------------------------------------%>
							<%-------------Fourth Panel: Search Button----------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-1">
								<div class="panel noborder">
									<div class="panel-body">
										<div class="form-group-sm">
											<%---------Search Button-------------%>
											<br/>
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

<wrxj:ajaxTable metaDataName="Recovery" ajaxUri="/airflowwcs/recovery2/list" hasAutoRefresh="true" hasColVis="true"
	prefHideColumns="${userPref.tableColumnVisibility['Recovery']}"
	metaId="TrayID"  hasRefresh="true" hasFilter="true" hasAdd="false"></wrxj:ajaxTable>

<%@include file="../_template/alertsFloating.jspf"%>
<%@include file="recoveryItemDetails.jspf" %>
<%@include file="recoveryMoves.jspf" %>
<%@include file="recoveryLocations.jspf" %>
<%-- Include this script if the user has elevated privileges.  This must be included
	 before the standard javascript file. --%>
<security:authorize access="hasAnyRole('ROLE_ELEVATED', 'ROLE_ADMIN', 'ROLE_MASTER')">
	<script src="<spring:url value="/resources/js/recovery-inventory-elevated.js"/>" type="text/javascript"></script>
</security:authorize>
<script src="<spring:url value="/resources/js/recovery-inventory.js"/>" type="text/javascript"></script>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>