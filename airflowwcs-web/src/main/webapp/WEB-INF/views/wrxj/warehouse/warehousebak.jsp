<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>

<!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Load Screen">
<title>WAREHOUSE - Facility - Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

</head>
<body>
<!-- Static Include -->
<%@include file="../_template/nav.jspf"%>
<%@include file="../_template/alerts.jspf" %>
<wrxj:ajaxTable metaDataName="Warehouse" ajaxUri="/airflowwcs/table/ajax?screen=warehouse" 
				metaId="Warehouse" hasRefresh="true" hasFilter="true"  
				tableTitle="Warehouses" tableDescription="" hasSearch="true" hasColVis="true" ></wrxj:ajaxTable>


    
    
</body>
</html>