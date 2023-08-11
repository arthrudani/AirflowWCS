<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true"%>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>ALERTS | Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta name="description" content="WRXJ - Work Maintenance Screen" />
<meta charset="utf-8" />

<%-- Static Include --%>
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
<%@include file="../_template/date_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf"%>
<%@include file="../_template/executing.jspf"%>

<div id="divExecuting" style="margin: 0px; padding: 0px; position: fixed; right: 0px;
    top: 0px; width: 100%; height: 100%; background-color: #666666; z-index: 30001;
    opacity: .8; filter: alpha(opacity=70);display:none" >
    <p style="position: absolute; top: 30%; left: 45%; color: White;">
        Executing...<img src="<spring:url value="/resources/img/ajax-loading.gif"/>">
    </p>
</div>

<div class="container-fluid">
		<div class="row">
			<spring:url value="/alerts/listSearch" var="formUrl"/>
			<form:form cssClass="form-horizontal" id="load-filtering-form" action="${formUrl}" method="POST" modelAttribute="alertDataModel">
				<div class="panel panel-default">
					<div class="panel-heading">
				        <h4 class="panel-title">
				          <a data-toggle="collapse" href="#collapseCriteria">Search Criteria</a>
				        </h4>
						<div class="clearfix"></div>							      
					</div>
					<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
					<div class="row">
						<div class="col-sm-5" >
						<!-------------------------------------------------------->
						<!-------First Panel: Description Info----------------------->
						<!-------------------------------------------------------->
							<div class="panel panel-default">
								<div class="panel-body">
						<!---------Description Fields------------->
									<div class="form-group-sm">
										<div class="col-sm-5">
										<span class="input-group-addon">Description</span>
											<form:input path="description" cssClass="form-control" id="description" placeholder="Description"/>
										</div>
									 </div>	
								</div>
							</div>
						</div>
						
					<!-------------SECOND Panel: Search Button---------------->
					<!-------------------------------------------------------->
					<div class="col-sm-5">
						<div class="panel noborder">
							<div class="panel-body">						
								<div class="form-group-sm">									
							<!---------Search Button------------->
									<div class="controls col-sm-5">
										<center>
											<button id="searchAlertsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
										</center>
									</div>
										
						<!---------Slave Board Field------------->	
									
								</div>											
							</div>
						
						</div>						
					</div>
					<!---------End of Third panel------------->
				</div>
<!---------End of First Row------------->	
			</div>
			</div>
		</div>							
	</form:form>
	</div>

</div>

<%-- <security:authorize access="hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MASTER')"> --%>
<%-- 		<wrxj:ajaxTable metaDataName="Alert" ajaxUri="/airflowwcs/alerts/list" --%>
<%-- 				metaId="Alert" hasRefresh="true" hasFilter="true" hasAdd="true"  --%>
<%-- 				hasAutoRefresh="true" hasExcel="true" hasColVis="true" hasSearch="true" numPageLength="10"></wrxj:ajaxTable> --%>
<%-- </security:authorize> --%>
	
	
<wrxj:ajaxTable metaDataName="Alert" ajaxUri="/airflowwcs/alerts/list"
	hasAutoRefresh="true" refreshRateSec="7" metaId="Alert"
	hasRefresh="true" hasFilter="true" hasColVis="true" theme="inverse" numPageLength="10" hasOnAll="true"></wrxj:ajaxTable>
	
<%@include file="../_template/alertsFloatingHeader.jspf"%>
<script src="<spring:url value="/resources/js/alert.js"/>" type="text/javascript"></script>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
	<script type="text/javascript">
		isAdmin = true;
	</script>
</security:authorize>

<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf"%>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf"%>
	</c:otherwise>
</c:choose>
</html>