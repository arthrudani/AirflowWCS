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
<title>WRX SWING ROLES | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<%@include file="../_template/alerts.jspf" %>
<security:authorize access="hasAnyRole('ROLE_MASTER')">

<wrxj:ajaxTable metaDataName="Role" ajaxUri="/airflowwcs/role/list" hasColVis="true"  prefHideColumns="${userPref.tableColumnVisibility['Role']}"
				metaId="Role" hasRefresh="true" hasFilter="true" hasAdd="true" hasDelete="true" hasEdit="true"></wrxj:ajaxTable>


    
<%@include file="wrxRoleAddPopover.jspf"%>    
<%@include file="wrxRoleDeletePopover.jspf"%>    
<%@include file="wrxRoleModifyPopover.jspf"%>   
<%@include file="wrxRoleOptionPopover.jspf"%>   
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<script src="<spring:url value="/resources/js/wrxrole.js"/>"
	type="text/javascript"></script>
<!-- HAS ADMIN ROLE, ROLE ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasRole('ROLE_ADMIN')">
 <script type="text/javascript">
 	isAdmin=true;
 </script>
</security:authorize>
</html>