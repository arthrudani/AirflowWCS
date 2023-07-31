<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>EQUIPMENT | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="WRXJ - Equipment Monitor">

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>

	<style>
		/* shrink right-click context menu dividers */
		li.is-divider hr {margin-top: 2px; margin-bottom: 2px;}
		/* legends */
		#legend-area div.panel-heading {padding: 15px 10px 5px 10px;}
		#legend-area li {padding: 5px 10px 5px 10px;}
	</style>
</head>
<%@include file="../_template/navBodyWrapper.jspf"%> 

<%@include file="../_template/confirmDialog.jspf"%>
<%@include file="../_template/alertsFloatingHeader.jspf"%>
<%@include file="equipmentSendBarcode.jspf"%>
<%@include file="equipmentErrorGuidance.jspf"%>

<!-- Equipment Monitor Panel -->
<div id="equipment-layout" class="easyui-layout" style="width:calc( 100% - 100px );height:890px;visibility:hidden;">
	<%@include file="equipmentUiEast.jspf"%>
	<%@include file="equipmentUiWest.jspf"%>
	<%@include file="equipmentUiCenter.jspf"%>
</div>

<script src="<spring:url value="/resources/assets/js/d3.js"/>" charset="utf-8"></script>
<%@include file="../_template/additional_ui_scripts.jspf"%>
<security:authorize access="hasAnyRole('ROLE_ELEVATED','ROLE_ADMIN','ROLE_MASTER')">
	<script src="<spring:url value="/resources/js/equipment-elevated.js"/>" type="text/javascript"></script>
</security:authorize>
<script src="<spring:url value="/resources/js/equipment.js"/>" type="text/javascript"></script>

<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf"%>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf"%>
	</c:otherwise>
</c:choose>
<!-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
<script type="text/javascript">
      isAdmin=true;
</script>
</security:authorize>
</html>