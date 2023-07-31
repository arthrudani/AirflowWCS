<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<!-- Tag Libraries -->
<%@ taglib uri="wrxj-taglib" prefix="wrxj"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - System Configuration">
<title>SYSTEM CONFIGURATION | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>

<!-- Messaging Include -->
<script src="<spring:url value="/resources/assets/js/stomp.js"/>"
	type="text/javascript"></script>
<script src="<spring:url value="/resources/assets/js/sockjs.js"/>"
	type="text/javascript"></script>

<script src="<spring:url value="/resources/js/sysconfig.js"/>"
	type="text/javascript"></script>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">

<div id="divExecuting" style="margin: 0px; padding: 0px; position: fixed; right: 0px;
    top: 0px; width: 100%; height: 100%; background-color: #666666; z-index: 30001;
    opacity: .8; filter: alpha(opacity=70);display:none" >
    <p style="position: absolute; top: 30%; left: 45%; color: White;">
        Executing...<img src="<spring:url value="/resources/img/ajax-loading.gif"/>">
    </p>
</div>


<div class="inner-content">
<div class="col-md-12">
	<div class="row">
		<div class="col-md-12">
			<div class="row" id="sysConfigTabs" data-role="tab">
				<ul class="nav nav-tabs">
					<li class="active"><a href="#controllerTab" 
						data-toggle="tab" id="controllerTabToggle">Controller Configs</a>
					</li>
					<li class=""><a data-toggle="tab" href="#sysConfigTab" 
						id="sysConfigTabToggle">System Configs</a>
					</li>
				</ul>
					<div class="tab-content clearfix">
						<div class="tab-pane active" id="controllerTab">
													
								<wrxj:ajaxTable metaDataName="ControllerConfig"
									ajaxUri="/airflowwcs/sysconfig/listControllerConfig" tableId="CCTable" metaId="Property Name"
									hasRefresh="true" inModal="true" hasFilter="true"></wrxj:ajaxTable>
				
						<div class="tab-pane" id="sysConfigTab">

								<wrxj:ajaxTable metaDataName="SysConfig"
									ajaxUri="/airflowwcs/sysconfig/listSysConfig" tableId="SysConfigTable" metaId="Parameter Name"
									hasRefresh="true" inModal="true" hasFilter="true"></wrxj:ajaxTable>
		
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	
</div>

</div>
<%@include file="sysconfigModify.jspf"%>
<%@include file="../_template/navBodyWrapperEnd.jspf"%>
</security:authorize>
	<c:choose>
		<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf" %>
		</c:when>
		<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf" %>
		</c:otherwise>
	</c:choose>
<%@include file="../_template/alertsFloating.jspf"%>	

<script src="<spring:url value="/resources/js/jms.js"/>"
	type="text/javascript"></script>
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
 <script type="text/javascript">
 	isAdmin=true;
 </script>
</security:authorize>
</html>