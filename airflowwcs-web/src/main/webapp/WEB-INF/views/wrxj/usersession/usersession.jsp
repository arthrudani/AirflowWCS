<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>USER SESSIONS | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Device"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/alertsFloatingHeader.jspf" %>
<%@include file="../_template/executing.jspf" %>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
	<div class="row">
		<div class="margin-border-10">
			<div class="col-sm-4">
				<div class="panel-group col-sm-12">
					<div class="panel panel-default">
						<div class="panel-heading">
							<center>
								<h4>Server Activity Details</h4>
							</center>
						</div>
						<div id="server-activity-body" class="panel-body">
							<div class="col-sm-12">
								<div class="row">
									<div class="col-sm-10">
										<h4 class="card-title pull-right">Total Active Sessions(s):</h4>
										<p class="text-muted pull-right">The number of concurrent logins originating from client machines.</p>
									</div>
									<div class="cols-sm-2"><h4 class="card-title"><span id="num-active-login">0</span></h4></div>
								</div>
								<div class="row">
									<div class="col-sm-10">
										<h4 class="card-title pull-right">Administrator Sessions(s):</h4>
										<p class="text-muted pull-right">The number of logins with ROLE_ADMIN and ROLE_MASTER.</p>
									</div>
									<div class="cols-sm-2"><h4 class="card-title pull-left"><span id="num-admin-login" style="color:green">0</span></h4></div>
								</div>
								<div class="row">
									<div class="col-sm-10" >
										<h4 class="card-title pull-right">Distinct Active User(s):</h4>
										<p class="text-muted pull-right">The number of unique User Accounts currently active</p>
									</div>
									<div class="cols-sm-2"><h4 class="card-title "><span id="num-unique-login" style="color:orange">0</span></h4></div>
								</div>
								<div class="row">
									<div class="col-sm-10" >
										<h4 class="card-title pull-right"> Duplicate User Sessions(s):</h4>
										<p class="text-muted pull-right">The number of User sessions with duplicate User ID's</p>
									</div>
									<div class="cols-sm-2"><h4 class="card-title "><span id="num-dup-login" style="color:red">0</span></h4></div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-8">
				<div class="panel panel-default">
					<div class="panel-heading"><center>
						<h4>Activity</h4></center>
					</div>
					<div class="panel-body">
						<wrxj:hibernateAjaxTable ajaxUri="/airflowwcs/usersession/list" regexHighlightList="${regexHighlights}"
							tableId="Sessions" hasRefresh="true" hasFilter="true" hasAutoRefresh="true"
							hasColVis="true" explicitColumns="${sessionColumns}"
							prefHideColumns="${userPref.tableColumnVisibility['UserSessionManagement']}"
							metaId="IP" inModal="true"/>
					</div>
				</div>
			</div>
		</div>
	</div>

	<script src="<spring:url value="/resources/js/userSession.js"/>" type="text/javascript"></script>
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>