<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>HOST LOG VIEWER | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Host Log Viewer Screen"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
	<%@include file="../_template/date_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/executing.jspf" %>

<div class="container-fluid">
	<div class="row">
		<spring:url value="/hostlogview/listSearch" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="history-filtering-form" action="${formUrl}" method="POST" modelAttribute="historyDisplay">
			<div class="panel panel-default">
				<%@include file="../_template/searchHeader.jspf" %>
				<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
						<div class="row">
							<%--------------------------------------------------------%>
							<%------- Search Info ------------------------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-6" >
								<div class="panel panel-default">
									<div class="panel-body">
										<div class="form-group-sm">
											<div class="col-sm-6">
												<form:input path="startingDate" cssClass="form-control" id="scStarting" placeholder="Starting Date" style="display:none;"/>
												<form:input path="endingDate" cssClass="form-control" id="scEnding" placeholder="Ending Date" style="display:none;"/>
												<label for="scRange">Date Range</label>
												<input type="text" id="scRange" class="form-control"/>
											</div>
										</div>
										<div class="form-group-sm">
											<div class="col-sm-6">
												<label for="scData">Filter</label>
												<form:input path="data" cssClass="form-control" id="scData" placeholder="Filter"/>
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
										<%---------Search Button-------------%>
										<br/>
										<center>
											<button id="searchHistoryButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
										</center>
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

<wrxj:ajaxTable metaDataName="WRxHostLog" ajaxUri="/airflowwcs/hostlogview/empty" hasAutoRefresh="false" refreshRateSec="7"
	prefHideColumns="${userPrefs.tableColumnVisibility['WRxHostLog']}"
	metaId="Timestamp" hasRefresh="true" hasFilter="true"
	theme="inverse" numPageLength="100"/>

<script src="<spring:url value="/resources/js/hostlogview.js"/>" type="text/javascript"></script>

<%@include file="../_template/alertsFloatingHeader.jspf"%>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
<%-- HAS ADMIN ROLE, ORDER MAINT ADMIN SPECIFIC JAVASCRIPT --%>
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