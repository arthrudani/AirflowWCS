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
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Container">
<title>STORE | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

<!-- Messaging Include -->
<script src="<spring:url value="/resources/assets/js/stomp.js"/>" type="text/javascript"></script>
<script src="<spring:url value="/resources/assets/js/sockjs.js"/>" type="text/javascript"></script>

</head>
<%@include file="../_template/navBodyWrapper.jspf"%>

<security:authorize access="hasRole('ROLE_USER')">
<spring:url value="/store/find" var="formUrl" />
<spring:url value="/pick/view" var="pickScreenUrl" />
<form:form cssClass="form-horizontal" id="load-add-form"
	action="${formUrl}" method="POST" modelAttribute="storeModel">

	<div class="content-title">
		<h3 class="main-title">Store</h3>
		<span>Select a station to begin.</span>
	</div>

	<div class="inner-content">
		
		<!-- parameter panel start -->
		<div class="panel panel-default">
			<!-- panel body start -->
  			<div class="panel-body">
  			
  
    			 	<div class="col-sm-4 pad-10-top">
						<label for="station">Station</label>
							<form:select path="station" cssClass="form-control" id="station"
								required="required" >
								<form:option id="station-placeholder" value=""></form:option>
								<form:options items="${dropdownMenus.stations}" />
								</form:select>
					</div>
					<div class="col-sm-2 pad-10-top">
						<label for="container">Container Type</label>
						<form:select path="containerType" cssClass="form-control"
							id="container" >
							<form:option id="container-placeholder" value=""></form:option>
							<form:options items="${dropdownMenus.containerTypes}" />
						</form:select>
					</div>


					<div class="col-sm-4 pad-10-top">
						<label for="loadId">Load ID</label>
						<form:input path="loadId" cssClass="form-control" id="loadId"
							value="" readonly="true"/>
					</div>
					<div class="col-sm-4 pad-10-top">
						<label for="expectedReceipt">Expected Receipt</label>
						<form:input path="expectedReceipt" cssClass="form-control"
							id="expectedReceipt" value="" readonly="true"/>
					</div>
					<div class="col-md-3 pad-20-top">
							<button id="releaseLoadButton" 
									class="btn btn-warning btn-lg btn-block btn-huge" 
									type="button" 
									disabled="disabled">Release Load</button>
        			</div>
        			<div class="col-md-3 pad-20-top">
            				<a id="pickScreenButton" href="${pickScreenUrl}"
            						class="btn btn-primary btn-lg btn-block btn-huge">Pick Screen</a>
        			</div>

			</div>
			<!-- panel body end -->
  		</div>
  		<!-- parameter panel end -->
	<%@include file="../_template/alerts.jspf"%>

</form:form>


	<wrxj:ajaxTable metaDataName="Store Items"
		ajaxUri="/airflowwcs/store/empty" metaId="Item ID" hasRefresh="true"
		hasFilter="true" hasAdd="true"></wrxj:ajaxTable>

</div> <!--  this is actually correct because of the body wrapper -->
<%@include file="releaseStore.jspf"%>
<%@include file="addItemStore.jspf"%>
<%@include file="expectedReceiptStore.jspf"%>
</security:authorize>
<script src="<spring:url value="/resources/js/store.js"/>" type="text/javascript"></script> <!-- needs to be placed after ajaxTable to access the DataTables object -->
<%@include file="../_template/navBodyWrapperEnd.jspf"%>
</html>