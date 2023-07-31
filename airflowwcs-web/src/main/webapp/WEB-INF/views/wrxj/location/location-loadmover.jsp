<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>LOCATION | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Load Screen"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/executing.jspf" %>
<%@include file="../_template/alertsFloatingHeader.jspf"%>

<%@include file="locationDetails-loadmover.jspf" %>

<div class="container-fluid">
	<div class="row">
		<spring:url value="/location/listSearch" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="location-filtering-form" action="${formUrl}" method="POST" modelAttribute="locationModel">
			<div class="panel panel-default">
				<%@include file="../_template/searchHeader.jspf" %>
				<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
						<div class="row">
							<%--------------------------------------------------------%>
							<%------- Location Search --------------------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-10" id="scPanel">
								<div class="panel panel-default">
									<div class="panel-body">
										<div class="row">
											<%------- Location -------%>
											<div class="form-group-sm col-sm-4">
												<label for="warehouse" class="control-label">Location</label>
												<div class="input-group">
													<form:select path="warehouse" cssClass="form-control" id="scWarehouse" style="min-width:65px;">
														<form:options items="${dropdownMenus.warehouses}" />
													</form:select>
													<div class="input-group-addon">-</div>
													<form:input path="address" cssClass="form-control" id="scAddress" placeholder="Address" style="min-width:65px;"/>
												</div>
											</div>
											<%------- Device -------%>
											<div class="form-group-sm col-sm-2">
												<label for="scDeviceId" class="control-label">Device</label>
												<form:select path="deviceId" cssClass="form-control" id="scDeviceId">
													<form:options items="${dropdownMenus.devices}" />
												</form:select>
											</div>
											<%------- Zone -------%>
											<div class="form-group-sm col-sm-2">
											<label for="scZone" class="control-label">Zone</label>
											<form:select path="zone" cssClass="form-control" id="scZone">
												<form:options items="${dropdownMenus.zones}" />
											</form:select>
											</div>
											<%------- Location Status -------%>
											<div class="form-group-sm col-sm-2">
												<label for="scLocationStatus" class="control-label">Location&nbsp;Status</label>
												<form:select path="locationStatus" cssClass="form-control" id="scLocationStatus">
													<form:option value="<%=com.daifukuamerica.wrxj.util.SKDCConstants.ALL_INT%>" label="ALL" />
													<form:options items="${dropdownMenus.locStatuses}" />
												</form:select>
											</div>
											<%------- Empty Flag -------%>
											<div class="form-group-sm col-sm-2">
												<label for="scEmptyFlag" class="control-label">Empty&nbsp;Flag</label>
												<form:select path="emptyFlag" cssClass="form-control" id="scEmptyFlag">
													<form:option value="<%=com.daifukuamerica.wrxj.util.SKDCConstants.ALL_INT%>" label="ALL" />
													<form:options items="${dropdownMenus.locEmptyFlags}" />
												</form:select>
											</div>
											<%------- Type -------%>
											<div class="form-group-sm col-sm-2">
												<label for="scType" class="control-label">Type</label>
												<form:select path="type" cssClass="form-control" id="scType">
													<form:option value="<%=com.daifukuamerica.wrxj.util.SKDCConstants.ALL_INT%>" label="ALL" />
													<form:options items="${dropdownMenus.locType}" />
												</form:select>
											</div>
										</div>
									</div>
								</div>
							</div>
							
							<%--------------------------------------------------------%>
							<%------- Search Button ----------------------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-1">
								<div class="panel noborder">
									<div class="panel-body">
										<div class="form-group-sm">
											<%---------Search Button-------------%>
											<br/>
											<center>
												<button id="searchLocationsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
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
<script>
var test = "${userPref.tableColumnVisibility['Location']}";
</script>
<script src="<spring:url value="/resources/js/location.js"/>" type="text/javascript"></script>
<%-- MODIFY LOCATION for ELEVATED+ users --%>
<security:authorize access="hasAnyRole('ROLE_ELEVATED', 'ROLE_ADMIN', 'ROLE_MASTER')">
<%@include file="locationModify.jspf" %>
<script src="<spring:url value="/resources/js/location-elevated.js"/>" type="text/javascript"></script>
</security:authorize>

<wrxj:ajaxTable metaDataName="Location" ajaxUri="/airflowwcs/location/list"
	prefHideColumns="${userPref.tableColumnVisibility['Location']}"
	metaId="id" hasRefresh="true" hasFilter="true"
	hasSearch="false" hasColVis="true" numPageLength="30"/>

<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>