<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>USERS | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Container"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>
<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/confirmDialog.jspf" %>
<%@include file="../_template/alertsFloatingHeader.jspf" %>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
	<% String[] hideUserCols = {"Role","Release To Code","Expiration"}; %>
	<wrxj:ajaxTable metaDataName="Employee" ajaxUri="/airflowwcs/users/list" hideColumns="<%=hideUserCols%>"
		metaId="User ID" hasRefresh="true" hasFilter="true" hasAdd="true" />
	
	<script src="<spring:url value="/resources/js/userManagement.js"/>" type="text/javascript"></script>
	<%@include file="userAdd.jspf" %> 
	<%@include file="userModify.jspf" %> 
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>