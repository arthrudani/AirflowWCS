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
<title>ITEM DETAILS - Inventory - Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>


</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<%@include file="../_template/alerts.jspf" %>
	<security:authorize access="hasRole('ROLE_ADMIN')">
		<wrxj:ajaxTable metaDataName="ItemDetail" ajaxUri="/airflowwcs/itemdetail/list" 
				metaId="ItemDetail" hasRefresh="true" hasFilter="true" prefHideColumns="${userPref.tableColumnVisibility['ItemDetail']}"
				hasAutoRefresh="true" hasExcel="true" hasColVis="true" hasSearch="true" numPageLength="100"></wrxj:ajaxTable>
	</security:authorize>
	
	<security:authorize access="hasRole('ROLE_USER') and !hasRole('ROLE_ADMIN')">
	<wrxj:ajaxTable metaDataName="ItemDetail" ajaxUri="/airflowwcs/itemdetail/list"  prefHideColumns="${userPref.tableColumnVisibility['ItemDetail']}"
				metaId="ItemDetail" hasRefresh="true" hasFilter="true" hasSearch="true"></wrxj:ajaxTable>
	</security:authorize>
	
	<security:authorize access="hasRole('ROLE_READONLY')">
	<wrxj:ajaxTable metaDataName="ItemDetail" ajaxUri="/airflowwcs/itemdetail/list"  prefHideColumns="${userPref.tableColumnVisibility['ItemDetail']}"
				metaId="ItemDetail" hasRefresh="true" hasFilter="true"  ></wrxj:ajaxTable>
	</security:authorize>				 

	<%@include file="itemAdd.jspf" %>
	<%@include file="itemDetailModify.jspf" %>
		
    
    
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<script src="<spring:url value="/resources/js/itemDetails.js"/>" type="text/javascript"></script>
<!-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasRole('ROLE_ADMIN')">
 <script type="text/javascript">
 	isAdmin=true;
 </script>
</security:authorize>
</html>