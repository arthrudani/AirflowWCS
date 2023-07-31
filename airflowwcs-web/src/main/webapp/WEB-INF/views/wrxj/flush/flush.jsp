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
<title>Flush Aisles - Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ">

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>

<!-- Messaging Include -->
<script src="<spring:url value="/resources/assets/js/stomp.js"/>"
	type="text/javascript"></script>
<script src="<spring:url value="/resources/assets/js/sockjs.js"/>"
	type="text/javascript"></script>


</head>
<%@include file="../_template/navBodyWrapper.jspf"%>

<%@include file="../_template/table_scripts.jspf"%>


<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER', 'ROLE_ELEVATED')">
<div class="inner-content">
	<div class="container-fluid">
		<div class="row">
		<form:form method="POST" id="flush-form" cssClass="form-horizontal" >
		<div class="container-fluid shadow">
		<div class="row">
			<div class="row">
				<div class="col-md-12">
					<div class="row">
						<div class="col-md-4">
							<div class="panel panel-default">
								<div class="panel-heading">
										<h3 class="panel-title pull-left">Flush Loads from Aisle(s)</h3>
										<div class="clearfix"></div>
								</div>
								<div class="panel-body">
									<div class="form-group brdbot" style="display: block;">
											<%-- <center>
												<h4>Station</h4>
											</center> --%>
									</div>
<!-------------------------------------------------------->
<!------------------Stations Dropdown--------------------->
<!-------------------------------------------------------->
									<div class="form-group">
										<label class="control-label control-label-left col-sm-3" for="srcAisle">Aisles</label>
										<div class="controls col-sm-9">									
											<div class="input-group">
											<span class="input-group-addon" ><i class="fa fa-unlock" aria-hidden="true" id="lock-status"></i></span>
												<select id="srcAisle" class="form-control" data-role="select">
													<option hidden>Choose an Aisle...</option>
													<c:forEach items="${dropdownMenus.srcaisles}"
														var="aisleEach">
														<option val="${aisleEach}">${aisleEach}</option>
													</c:forEach>
												</select>
											</div>											
										</div>
									</div>	
									<div></div>

<!-------------------------------------------------------->
<!------------------Get Weight Button--------------------->
<!-------------------------------------------------------->	
									<div class="form-group">
										<center><button id="flushAisleButton" type="button"
												class="btn btn-warning btn-default">Flush Loads</button>
										</center>
									</div>	
									<div></div>					
							</div>	
						</div>
					</div>
				</div>
			</div>
			</div>
			</div>
			</div>
			</form:form>
		</div>
	</div>
</div>
</security:authorize>
<!-- Tabbed panel end -->


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
<script src="<spring:url value="/resources/js/flush.js"/>"
	type="text/javascript"></script>
</html>