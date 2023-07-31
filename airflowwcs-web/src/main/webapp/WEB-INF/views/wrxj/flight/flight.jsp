<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Flights">
<title>FLIGHTS | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_USER')">
		
<wrxj:ajaxTable metaDataName="Flight" ajaxUri="/airflowwcs/flight/list" prefHideColumns="${userPref.tableColumnVisibility['Flight']}" 
				metaId="Flight" hasRefresh="true" hasAutoRefresh="true" hasFilter="true" tableId="Flights"></wrxj:ajaxTable>																
</security:authorize>

<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<%@include file="flightRetrieve.jspf" %>
<script src="<spring:url value="/resources/js/flight.js"/>" type="text/javascript"></script>
<!-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasRole('ROLE_ADMIN')">
 <script type="text/javascript">
 	isAdmin=true;
 </script>
</security:authorize>
</html>