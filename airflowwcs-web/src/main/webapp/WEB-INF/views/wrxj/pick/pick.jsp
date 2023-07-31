<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<!-- Tag Libraries -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta charset="utf-8">
<title>PICK - Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ">

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>

<!-- Messaging Include -->
<script src="<spring:url value="/resources/assets/js/stomp.js"/>" type="text/javascript"></script>
<script src="<spring:url value="/resources/assets/js/sockjs.js"/>" type="text/javascript"></script>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<%@include file="../_template/table_scripts.jspf"%>

<security:authorize access="hasRole('ROLE_USER')">


	<div class="inner-content">

		<!-- parameter panel start -->
		<div class="panel panel-default">
			<!-- panel body start -->
  			<div class="panel-body">
				<div class="row">

					<div class="col-md-6">
						<div class="input-group">
							<span class="input-group-addon" id="stationview-addon">STATION</span>
							<select id="station" class="form-control input-lg" >
								<option hidden>Choose a Station...</option>
								<c:forEach items="${dropdownMenus.stations}" var="stationEach">
								<option val="${stationEach}">${stationEach}</option>
								</c:forEach>
							</select>
						</div>
					</div>
					<div class="col-sm-4 ">
					    	<div class="input-group">
								<span class="input-group-addon" id="loadview-addon">LOAD</span>
								<input id="viewLoadId"  value="" class="form-control input-lg" readonly/>
							</div>
					</div>
				</div>													
			</div>
  			<!-- panel body end -->
  		</div>
  		<%@include file="../_template/alerts.jspf" %>
  		
  		<spring:url value="/pick/completePick" var="formUrl" />
  		<form:form cssClass="form-horizontal" id="pick-form"  method="POST" modelAttribute="moveData" action="${formUrl}">
  		<div class="panel panel-default">
			<!-- panel body start -->
  			<div class="panel-body">
				<div class="row">

					    <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="order-addon">ORDER ID</span>
								<form:input id="orderID" path="orderID" value="" 
											readonly="true" cssClass="form-control input-lg"
											aria-describedby="order-addon"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="location-addon">LOCATION</span>
								<form:input id="location" path="address" value="" cssClass="form-control input-lg" 
											readonly="true" aria-describedby="location-addon"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="pickfrom-addon">PICK FROM LOAD</span>
								<form:input id="pickFromLoad" path="loadID" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="itemdescription-addon">ITEM DESCRIPTION</span>
								<form:input id="itemDescription" path="item" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="item-addon">ITEM</span>
								<form:input id="item" path="item" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="lot-addon">LOT</span>
								<form:input id="pickLot" path="pickLot" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group"> <!-- TODO get the sublocation for this pick -->
								<span class="input-group-addon" id="subloc-addon">SUB-LOCATION</span>
								<form:input id="subLocation" path="address" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>
						 <div class="col-sm-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="pickqty-addon">PICK QUANTITY</span>
								<form:input id="pickQuantity" path="pickQuantity" value="" cssClass="form-control input-lg" readonly="true"/>
							</div>
						</div>

					
				</div>
			</div>
  			<!-- panel body end -->
  		</div>
  		
  		<!-- parameter panel end -->
  		<div class="panel panel-default">
			<!-- panel body start -->
  			<div class="panel-body">
				<div class="row">
						<div class="col-md-4 pad-10-top">
					    	<div class="input-group">
								<span class="input-group-addon" id="confirmPick-addon">CONFIRM PICK QUANTITY</span>

									<form:input id="confirmPickQuantity" path="confirmPickQuantity" cssClass="form-control input-lg" value="" readonly="true" required="required"/>

							</div>
						</div>
						<div class="col-md-3 ">
            				<button id="pickScreenButton" 
            						class="btn btn-primary btn-lg btn-block btn-huge" 
            						type="button" disabled="disabled">Confirm Pick</button>
        				</div>
						<div class="col-md-3">
							<button id="releaseLoadButton" 
									class="btn btn-warning btn-lg btn-block btn-huge" 
									type="button" 
									disabled="disabled">Release Load</button>
        				</div>
        				
				</div>
			</div>
		</div>
		<form:hidden path="parentLoad" value="" id="parentLoad"/>
<%-- 		<form:hidden path="pickToLoadID" value="" id="pickToLoadID"/> --%>
		<form:hidden path="orderLot" value="${ipAddress}" id="orderLot"/>
		<form:hidden path="schedulerName" value="" id="schedulerName"/>
		<form:hidden path="routeID" value="" id="routeID"/>
		<form:hidden path="deviceID" value="" id="deviceID"/>
		<form:hidden path="releaseToCode" value="" id="releaseToCode"/>
		<form:hidden path="lineID" value="" id="lineID"/>
		<form:hidden path="destWarehouse" value="" id="destWarehouse"/>
		<form:hidden path="nextWarehouse" value="" id="nextWarehouse"/>
		<form:hidden path="nextAddress" value="" id="nextAddress"/>
		<form:hidden path="warehouse" value="" id="warehouse"/>
		<form:hidden path="displayMessage" value="" id="displayMessage"/>
		<form:hidden path="positionID" value="" id="positionID"/>
		<form:hidden path="moveDate" value="" id="moveDate"/>
		<form:hidden path="moveID" value="" id="moveID"/>
		<form:hidden path="aisleGroup" value="" id="aisleGroup"/>
		<form:hidden path="moveSequence" value="" id="moveSequence"/>
		<form:hidden path="priority" value="" id="priority"/>
		<form:hidden path="moveType" value="" id="moveType"/>
		<form:hidden path="moveCategory" value="" id="moveCategory"/>
		<form:hidden path="moveStatus" value="" id="moveStatus"/>
		</form:form> 
		<div class="panel panel-default">
			<!-- panel body start -->
  			<div class="panel-body">
				<div class="row">
						<wrxj:ajaxTable metaDataName="Move"
							ajaxUri="/airflowwcs/store/empty" metaId="Move ID" hasRefresh="true"
							hasFilter="true" ></wrxj:ajaxTable>		
				</div>
			</div>
		</div>

  	</div>

<%@include file="releasePick.jspf" %>
</security:authorize>
<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf" %>
<script src="<spring:url value="/resources/js/pick.js"/>" type="text/javascript"></script>
</html>