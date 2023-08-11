<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true"%>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>ALERTS | Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta name="description" content="WRXJ - Work Maintenance Screen" />
<meta charset="utf-8" />

<%-- Static Include --%>
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
<%@include file="../_template/date_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf"%>
<%@include file="../_template/executing.jspf"%>
<wrxj:ajaxTable metaDataName="Alert" ajaxUri="/airflowwcs/alerts/list"
	hasAutoRefresh="true" refreshRateSec="7" metaId="Alert"
	hasRefresh="true" hasFilter="true" hasColVis="true" theme="inverse" numPageLength="10" ></wrxj:ajaxTable>
	
<%@include file="../_template/alertsFloatingHeader.jspf"%>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
	<script type="text/javascript">
		isAdmin = true;
	</script>
</security:authorize>

<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf"%>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf"%>
	</c:otherwise>
</c:choose>
</html>