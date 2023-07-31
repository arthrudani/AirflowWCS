<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>DEVICES | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="WRXJ - Device">

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/executing.jspf" %>
<%@include file="../_template/alertsFloatingHeader.jspf"%>

<wrxj:ajaxTable metaDataName="Device" ajaxUri="/airflowwcs/device/list" regexHighlightList="${regexHighlights}"
	metaId="Device ID" hasRefresh="true" hasFilter="true" hasColVis="true"  prefHideColumns="${userPref.tableColumnVisibility['Device']}" />

<%-- MODIFY SHELF for ELEVATED+ users --%>
<security:authorize access="hasAnyRole('ROLE_ELEVATED', 'ROLE_ADMIN', 'ROLE_MASTER')">
	<%@include file="deviceModify.jspf"%>
	<script src="<spring:url value="/resources/js/device-elevated.js"/>" type="text/javascript"></script>
</security:authorize>
    
<%@include file="../_template/navBodyWrapperEnd.jspf" %>

</html>