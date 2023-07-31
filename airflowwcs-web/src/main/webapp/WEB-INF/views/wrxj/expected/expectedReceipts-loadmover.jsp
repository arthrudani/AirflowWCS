<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>EXPECTED RECEIPTS | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Purchase Order / Expected Receipts Screen"/>
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
		<spring:url value="/expected/listSearch" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="order-filtering-form" action="${formUrl}" method="POST" modelAttribute="orderDisplay">
			<div class="panel panel-default">
				<%@include file="../_template/searchHeader.jspf" %>
				<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
						<div class="row">
							<%--------------------------------------------------------%>
							<%------- Order Info -------------------------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-4" >
								<div class="panel panel-default">
									<div class="row panel-body">
										<%---------Order ID Field-------------%>
										<div class="col-sm-6">
											<label for="scOrderId" class="control-label">Order ID</label>
											<form:input path="orderId" cssClass="form-control" id="scOrderId" placeholder="Order ID"/>
										</div>
										<%---------Load ID Field-------------%>
										<div class="col-sm-6">
											<label for="scLoadId" class="control-label">Load ID</label>
											<form:input path="loadId" cssClass="form-control" id="scLoadId" placeholder="Load ID"/>
										</div>
										<%---------Item ID Field-------------%>
										<div class="col-sm-6" style="display:none;">
											<label for="scItem" class="control-label">Item ID</label>
											<form:input path="item" cssClass="form-control" id="scItem" placeholder="Item ID"/>
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
												<button id="searchOrdersButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
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

<wrxj:ajaxTable metaDataName="PurchaseOrderHeader" ajaxUri="/airflowwcs/expected/empty" 
	prefHideColumns="${userPref.tableColumnVisibility['PurchaseOrderHeader']}"
	metaId="Expected Receipt ID" hasRefresh="true" hasFilter="true"   hasAutoRefresh="true" refreshRateSec="7"
	hasSearch="false" hasColVis="true"  theme="inverse" numPageLength="100"/>

<%@include file="deleteConfirmPopover.jspf" %>
<%@include file="erDetailPopover.jspf" %>

<script src="<spring:url value="/resources/js/expected.js"/>" type="text/javascript"></script>
<%@include file="../_template/alertsFloatingHeader.jspf"%>
<%-- HAS ADMIN ROLE, ORDER MAINT ADMIN SPECIFIC JAVASCRIPT --%>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
<script type="text/javascript">
	isAdmin=true;
</script>
</security:authorize>

<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf" %>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf" %>
	</c:otherwise>
</c:choose>
</html>
