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
							<%------- Search Info ------------------------------------%>
							<%--------------------------------------------------------%>
							<%---------Load ID-------------%>
							<div class="col-sm-1">
								<div class="form-group-sm">
									<label for="loadId" class="control-label">Load&nbsp;ID</label>
									<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Load ID"/>
								</div>
							</div>
							<%---------Tracking ID-------------%>	
							<div class="col-sm-1" style="display:none;">
								<div class="form-group-sm">
									<label for="mcKey" class="control-label">Tracking&nbsp;ID</label>
									<form:input path="mcKey" cssClass="form-control" id="mcKey" placeholder="Tracking ID"/>
								</div>
							</div>
							<%---------From Location-------------%>
							<div class="col-sm-2">
								<div class="form-group-sm">
									<label for="warehouse" class="control-label">From&nbsp;Location</label>
									<div class="input-group">
										<form:select path="warehouse" cssClass="form-control" id="warehouse" style="min-width:65px;">
											<form:option value="ALL"></form:option>
												<form:options items="${dropdownMenus.warehouses}" />
										</form:select>
										<div class="input-group-addon">-</div>
										<form:input path="address" cssClass="form-control" id="address" placeholder="Address" style="min-width:65px;"/>
									</div>
								</div>
							</div>	
							<%---------To Location-------------%>
							<div class="col-sm-2">
								<div class="form-group-sm">
									<label for="scwarehouse" class="control-label">To&nbsp;Location</label>
									<div class="input-group">
										<form:select path="nextWarehouse" cssClass="form-control" id="nextWarehouse" style="min-width:65px;">
											<form:option value="ALL"></form:option>
												<form:options items="${dropdownMenus.warehouses}" />
										</form:select>
										<div class="input-group-addon">-</div>
										<form:input path="nextAddress" cssClass="form-control" id="nextAddress" placeholder="Next Address" style="min-width:65px;"/>
									</div>
								</div>
							</div>
							<%---------Device ID Field-------------%>
							<div class="col-sm-1">
								<div class="form-group-sm">
									<label for="scdeviceId" class="control-label">Device</label>
									<form:select path="deviceId" cssClass="form-control" id="scdeviceId">
										<form:option value="ALL"></form:option>
											<form:options items="${dropdownMenus.deviceList}" />
									</form:select>
								</div>
							</div>
							<%-------------Search Button------------------------------%>
							<div class="col-sm-1">
								<div>
									<br/>
									<button id="searchLoadsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
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
	metaId="Load ID"  hasRefresh="true" hasFilter="true" hasAdd="false"></wrxj:ajaxTable>

<%@include file="../_template/alertsFloating.jspf"%>
<%@include file="recoveryMoves.jspf" %>
<%@include file="recoveryLocations.jspf" %>
<script src="<spring:url value="/resources/js/recovery-loadmover.js"/>" type="text/javascript"></script>
<%-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT --%>
<security:authorize access="hasRole('ROLE_ADMIN')">
	<script type="text/javascript">
		isAdmin = true;
	</script>
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>