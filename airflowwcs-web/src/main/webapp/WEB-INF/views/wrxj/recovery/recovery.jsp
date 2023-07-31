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
<title>RECOVERY | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<%@include file="../_template/alerts.jspf" %>
<div class="col-md-12">
	<div id="upper-selection-div" class="panel panel-default">
		<div class="panel-body">
		<div class="col-md-4">
			<div class="panel panel-default">
				<div class="panel-body">
				<div class="row">
				<div class="col-lg-12">
					<div class="col-lg-12">
					<h4><strong>Load</strong></h4>
					<input id="recover-load" class="form-control form-control-lg" type="text" placeholder="Select a load.." readonly>
					</div>
					<div class="col-lg-12">
						<div class="col-lg-6">
						<h6>Warehouse</h6>
							<input  id="recover-warehouse"class="form-control" type="text" placeholder="-" readonly>
						</div>
						<div class="col-lg-6">
							<h6>Address</h6>
							<input  id="recover-address" class="form-control" type="text" placeholder="-" readonly>
						</div>
						
					
					</div>
					<div class="col-lg-12">
						<div class="col-lg-6">
						<h6>Device</h6>
							<input id="recover-device" class="form-control" type="text" placeholder="-" readonly>
						</div>
						<div class="col-lg-6">
							<h6>Status</h6>
							<input  id="recover-status"class="form-control" type="text" placeholder="-" readonly>
							<input  id="recover-next-address"class="form-control" type="hidden" placeholder="-" readonly>
						</div>
						
					
					</div>
				
					
					
				</div>
				</div>
				<div class="cols-lg-4">
				
						
						<a class="btn btn-default pull-right margin-border-10" tabindex="0" id="recovery-view-load-details-button" aria-controls="ajaxTable" href="#load-details-collapse" data-toggle="collapse"><span ><i class="fa fa-eye  fa-fw" aria-hidden="true"></i>View Details</span></a>
						<a class="btn btn-default pull-right margin-border-10" tabindex="0" id="recovery-delete-load-button" aria-controls="ajaxTable" href="#"><span><i class="fa fa-trash  fa-fw" aria-hidden="true"></i>Delete</span></a>
						<a class="btn btn-default pull-right margin-border-10" tabindex="0" id="recovery-message-button" aria-controls="ajaxTable" href="#"><span><i class="fa fa-envelope  fa-fw" aria-hidden="true"></i>Message</span></a>
				
				</div>
					
				</div>
			</div>
		</div> 
		<div class="col-md-2">
			 <div class="col-lg-12">
				<div id="recovery-options-list" class="list-group">
				<h4>Recovery Type(s)</h4>
					<button type="button" class="list-group-item">None selected</button>
					
					
				</div>
			</div>
		</div>
		<div class="col-md-6">
		<div class="panel panel-default">
					
					<div class="panel-body"><center><h4>Recovery Method(s)</h4></center>
					<%@include file="recovery-none-selected.jspf" %>
					<%@include file="recovery-not-implemented.jspf" %>
					<%@include file="recovery-arrived.jspf" %>
					<%@include file="recovery-arrival-pending.jspf" %>
					<%@include file="recovery-bin.jspf" %>
					<%@include file="recovery-id-pending.jspf" %>
					<%@include file="recovery-ltw-move.jspf" %>
					<%@include file="recovery-ltw-schedule-pickup.jspf" %>
					<%@include file="recovery-ltw-schedule-deposit.jspf" %>
					<%@include file="recovery-ltw-reject.jspf" %>
					<%@include file="recovery-move-pending.jspf" %>
					<%@include file="recovery-move.jspf" %>
					<%@include file="recovery-prime-move.jspf" %>
					<%@include file="recovery-retrieve-pending.jspf" %>
					<%@include file="recovery-retrieve.jspf" %>
					<%@include file="recovery-store-pending.jspf" %>
					<%@include file="recovery-store.jspf" %>
					</div>
					
					<div class="clearfix"></div>
				</div>
		</div>
		</div>
	</div>
</div>
<div class="row"></div>
<div id="load-details-collapse" class="collapse fade col-lg-12">
	<div class="panel panel-default">
		<div class="panel-body">
			<div class="col-lg-12">
				<div class="row">
				<div class="col-lg-6">
					<div id="recovery-options-list" class="list-group">
					<h4>Order(s) on Load:</h4>
					<button type="button" class="list-group-item">None selected</button>
					
					
				</div>
				</div>
				<div class="col-lg-6">
					<h4>Items on Load:</h4>
						<div>
						 <%  String[] hideItemCols = {"Load ID","Allocatedquantity","Holdtype"}; %>
						<wrxj:ajaxTable metaDataName="ItemDetail" ajaxUri="/airflowwcs/table/empty" hideColumns="<%=hideItemCols%>" compact="true" hasFooter="false"
						metaId="Item" tableId="ItemDetail" inModal="true"></wrxj:ajaxTable>
						</div>
				</div>
			</div>
		</div>
	</div>

	
	
	
	</div>
<security:authorize access="hasAnyRole('ROLE_USER','ROLE_READONLY')">
<wrxj:ajaxTable metaDataName="Recovery" ajaxUri="/airflowwcs/recovery/list" hasAutoRefresh="true" hasColVis="true"  prefHideColumns="${userPref.tableColumnVisibility['Recovery']}"
				metaId="Load ID"  hasRefresh="true" hasFilter="true" hasAdd="false" ></wrxj:ajaxTable>
</security:authorize>
    
    
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<script src="<spring:url value="/resources/js/recovery.js"/>" type="text/javascript"></script>
</html>