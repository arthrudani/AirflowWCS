<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    <!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Container">
<title>MESSAGES | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
<!-- JMS Messaging scripts -->
<%@include file="../_template/message_scripts.jspf"%>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>
<!-- Static Include -->
<%@include file="../_template/alerts.jspf" %>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
<!-- Page specific JS -->
<script src="<spring:url value="/resources/js/message.js"/>" type="text/javascript"></script>
	

<div class="content-title">
	<h3 class="main-title">MESSAGES</h3>
	<span>View JMS messages received on this client over WebSocket.</span>
</div>
<div class="inner-content">
	<div class="theme-panel panel">
		<div class="panel-heading">
			<span class="panel-title"></span>
		</div>
		<div class="panel-body">
			<div class="row">
				<div class="col-md-12 ">
					<form class="form-inline">
						<div class="form-group">
							<button id="connect" class="btn btn-default" type="button">Connect</button>
							<button id="disconnect" class="btn btn-default" type="button"
								disabled="disabled">Disconnect</button>
						</div>
					</form>
				</div>
			</div>
			<div class="row space-bottom10">
				
					<div class="col-md-2">
						<select name="topic" id="message-type" class="form-control">
							<option></option>
							<option value="loadArrival">Buffer Clear</option>
						</select>
					</div>
					<div class="col-md-6">
						<input type="text" id="text" class="form-control"
							placeholder="Import message ..." disabled="disabled">
					</div>
					<div class="col-md-4">
						<button id="send-message" class="btn btn-default"  disabled="disabled">Send</button>
					</div>
				
			</div>
			<div class="row">
				<div class="col-md-12">
					<table id="conversation" class="table table-striped">
						<thead>
							<tr>
								<th width="15%">From</th>
								<th width="10%">Topic</th>
								<th width="50%">Message</th>
								<th width="25%">Time</th>
							</tr>
						</thead>
						<tbody id="messages">
						</tbody>
					</table>
				</div>
			</div>

		</div>
	</div>
</div>
<!--  -->

    
<%@include file="messageLoadArrivalPopover.jspf" %>
<%@include file="messageWeightPopover.jspf" %>
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>