<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>

<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8"/>
	<title>Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ"/>

	<%-- Static Includes --%>
	<%@include file="_template/header.jspf"%>
	<%@include file="_template/core_scripts.jspf"%>
</head>

<%@include file="_template/navBodyWrapper.jspf" %>
	<div class="content-title">
		<h3 class="main-title">Welcome ${user.userName}!</h3> 
		<span>Please select a screen to begin.</span>
	</div>	
	
	<div class="theme-panel panel panel-default">
		<!-- Favorites -->
		<wrxj:favPanel pretty="true"/>
		<!-- Favorites -->
	</div>
	</div>
<%@include file="_template/navBodyWrapperEnd.jspf" %>
</html>