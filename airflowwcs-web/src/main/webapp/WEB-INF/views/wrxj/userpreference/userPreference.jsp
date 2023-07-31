<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>USER PREFERENCES | Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Device">

	<%-- Static Include --%>
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>

<security:authorize access="hasAnyRole('ROLE_USER','ROLE_READONLY')">
<div class="container"> 
<div class="panel panel-default" id="user-preferences-div">
  <div class="panel-heading"><center><i class="fa fa-cogs fa-2x pull-left" aria-hidden="true"> </i><h3>User Preferences:<strong> ${user.userName}</strong></h3></center></div>
  <div class="panel-body">
  		<spring:url value="/userpreference/modify" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="load-add-form" action="${formUrl}" method="POST" modelAttribute="userPreference">
		
		
			<div class="panel-group col-lg-6 col-md-8 col-sm-12"> 
				<div class="panel panel-default">
					<div class="panel-heading"><center><h4>User Information</h4></center></div>
					<div class="panel-body">  
						<label for="userId" class="control-label">User ID</label>
	  					<form:input path="userId" cssClass="form-control pick-bold" id="userId" readonly="true" aria-describedby="userid-addon"/>
	  				</div>
					<div class="panel-body">   
						<label for="userName" class="control-label">User Name (display name)</label>
	  					<form:input path="userName" cssClass="form-control  pick-bold" id="userName" readonly="true" aria-describedby="username-addon"/>
	  				</div>
	  				<%--
					<div class="panel-body">  
						<label for="userRole" class="control-label">SWING UI Role (not used)</label>
	  					<form:input path="role" cssClass="form-control  pick-bold" id="userRole" aria-describedby="username-addon" readonly="true"/>
	  				</div>
	  				 --%>
				</div>
			</div>
			
			<div class="panel-group col-lg-6 col-md-8 col-sm-12">
				<div class="panel panel-default">
				<div class="panel-heading"><center><h4>Configurable Settings</h4></center></div>
					<div class="panel-body">  
						<label for="theme-dropdown" class="control-label">UI Theme</label>
	  					<form:select path="uiTheme" cssClass="form-control" id="theme-dropdown" aria-describedby="uitheme-addon">
	  										<form:option value="${userPreference.uiTheme}"><strong>${userPreference.uiTheme}</strong></form:option>
	     									<form:options items="${dropdownMenus.themes}" />
						</form:select>
	  				</div>
					<div class="panel-body">   
						<label for="sidebar-lock-dropdown" class="control-label">Auto-Collapse Sidebar</label>
						<form:select path="uiTheme" cssClass="form-control" id="sidebar-lock-dropdown" aria-describedby="uitheme-addon">
	  										<form:option value="${userPreference.messageBox}"><strong>${userPreference.messageBox}</strong></form:option>
	     									<form:options items="${dropdownMenus.messageOptions}" />
						</form:select>
						
	  				</div>
					<%--
	  				<ul class="list-group">
						<li class="list-group-item">
							<span class="debug-group-addon" id="debugaddon">DEBUG MODE</span>
							 <form:select path="hasDebugDescription" cssClass="form-control" id="debug-mode-dropdown" aria-describedby="debug-group-addon">
							 					<form:option value="${userPreference.hasDebugDescription}">${userPreference.hasDebugDescription}</form:option>
							 					<form:option value=""></form:option>
												<form:option id="enable" value="1">Enable...</form:option>
		     									<form:option id="disable" value="0">Disable...</form:option>
							</form:select> 
						</li>
						</ul>
					--%>
				</div>
			</div>
			<div class="panel-group col-sm-12">
				<div class="panel panel-default">
				<div class="panel-heading"><center><h4>User Permission Group(s)</h4></center></div>
					<div id="user-permission-body" class="panel-body"> 
					</div>
				</div>
			</div>
		</form:form>
	</div>
</div>
</div>
    
<script src="<spring:url value="/resources/js/userPreferences.js"/>" type="text/javascript"></script>    
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<%@include file="../_template/alertsFloatingHeader.jspf" %>
</html>