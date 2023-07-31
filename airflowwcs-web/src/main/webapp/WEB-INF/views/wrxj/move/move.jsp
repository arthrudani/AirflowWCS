<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>MOVE | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta name="description" content="WRXJ - Load Screen">
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<wrxj:ajaxTable metaDataName="Move" ajaxUri="/airflowwcs/move/list"
	prefHideColumns="${userPref.tableColumnVisibility['Move']}"
	metaId="Address" hasRefresh="true" hasFilter="true" hasAdd="false" 
	hasSearch="false" hasColVis="true" ></wrxj:ajaxTable>

<%@include file="../_template/alerts.jspf" %>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>