<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Time slot">
<title>Time Slot | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf"%>
<%@include file="../_template/alerts.jspf"%>
<%@include file="deleteConfirmPopover.jspf" %>
<security:authorize
	access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER','ROLE_USER')">

	<div class="inner-content">
		<div class="container-fluid">
			<div class="row">

				<div>
					<div class="col-sm-3 col-md-6">
						<div class="panel panel-default">
							<div class="panel-body">							
								<label id="lblSelectedSchema" class="control-label h4" for="srcSchema"> Time slots for Schema : </label>
								<wrxj:ajaxTable metaDataName="TimeSlot" ajaxUri="/airflowwcs/timeslot/empty"
								 metaId="Time Slot" hasRefresh="true" tableId="Timeslots"></wrxj:ajaxTable>
							</div>
						</div>
					</div>
					<div class="col-md-6">
						<form:form method="POST" id="timeslot-form" cssClass="form-horizontal">
							<div class="panel panel-default">
								<div class="panel-body">
									<div class="form-group">
										<label class="control-label col-sm-2" for="srcSchema">Schema</label>
										<div class="controls col-sm-10">
											<div class="input-group">
											<span class="input-group-addon" ><i class="fa fa-line-chart" aria-hidden="true" id="lock-status"></i></span>
												<select id="dropdownSchemas" class="form-control" >
													<c:forEach items="${dropdownMenus.schemas}"
														var="schemasEach">
														<option value="${schemasEach.key}">${schemasEach.value}</option>
													</c:forEach>
												</select>
											</div>
										</div>
									</div>
									<div class="form-group">
										<label class="control-label col-sm-2" for="timeslot"
											style="margin: 25px 0px 0px 0px">Time </label>
										<div class="controls col-sm-10">
											<div class="timerComponent">
												<div class="timeInput">
													<button id="buttomHoursUp">
														<i class="fa fa-angle-up" aria-hidden="true"></i>
													</button>
													<input type="text" id="txtHourValue" placeholder="01"
														value="01" onkeypress="return isNumber(event)"
														maxlength="2">
													<button id="buttomHoursDown">
														<i class="fa fa-angle-down" aria-hidden="true"></i>
													</button>
												</div>
												<span><b> : </b></span>
												<div class="timeInput">
													<button id="buttomMinUp">
														<i class="fa fa-angle-up"></i>
													</button>
													<input type="text" id="txtMinuteValue" placeholder="00"
														value="00" onkeypress="return isNumber(event)"
														maxlength="2">
													<button id="buttomMinDown">
														<i class="fa fa-angle-down" aria-hidden="true"></i>
													</button>
												</div>
												<div class="controls col-sm-2">
													<button id="addTimeslotButton" type="button"
														class="btn btn-warning btn-default">Add</button>
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
		</div>
	</div>

</security:authorize>

<%@include file="../_template/navBodyWrapperEnd.jspf"%>
<link rel="stylesheet" type="text/css"
	href="<spring:url value="/resources/css/timeslot.css"/>" />
<script src="<spring:url value="/resources/js/timeslot.js"/>"
	type="text/javascript"></script>
<script type="text/javascript">
 defaultSchemaId="1";
 </script>
</body>
</html>