<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
	<div id="add-modal" class="modal fade">

		<div class="modal-dialog modal-lg">

			<div class="modal-content">

				<div class="modal-header">

					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>

					<h4 class="modal-title">Add Command</h4>

				</div>

				<div class="modal-body" id="addModalBody">
				
				<!--  body here inserted by javascript calling server to bet the rest... -->
				<spring:url value="/work/add" var="formUrl"/>
				<form:form cssClass="form-horizontal" id="command-add-form" action="${formUrl}" method="POST" modelAttribute="workDataModel">

						<div class="form-group">
							<label class="control-label col-sm-2" for="loadid">Load ID:</label>
							<div class="col-sm-4">
								<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Enter Load ID" required="required"/>
							</div>
							<button type="button" class="btn btn-primary" id="searchLoad" >Search Load</button>
						</div>
						
						<div class="form-group">
						<label class="control-label col-sm-2" for="from">From:</label>
							<div class="col-sm-4">
								<form:input path="from" cssClass="form-control" id="from" placeholder="Enter From" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="weight">To Dest:</label>
							<div class="col-sm-4">
								<form:input path="toDest" cssClass="form-control" id="toDest" placeholder="Enter To Dest" required="required"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="moveType">moveType:</label>
							<div class="col-sm-4">
								<form:select path="moveType" cssClass="form-control col-sm-2" id="moveType">
     									<form:options items="${dropdownMenus.moveType}" />
								</form:select>
							</div>
							<label class="control-label col-sm-2" for="globalId">Global Id:</label>
							<div class="col-sm-4">
								<form:input path="globalId" cssClass="form-control" id="globalId" placeholder="Enter Global Id" required="required"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="deviceId">Device ID:</label>
							<div class="col-sm-4">
								<form:select path="deviceId" class="form-control" id="deviceId" items="${dropdownMenus.deviceList}"/>
							</div>
							<label class="control-label col-sm-2" for="status">Status:</label>
							<div class="col-sm-4">
								<form:select path="status" cssClass="form-control" id="status" items="${dropdownMenus.status}"/>
							</div>
						</div>
						<div class="form-group">
						<label class="control-label col-sm-2" for="ItemId">Item Id:</label>
							<div class="col-sm-4">
								<form:input path="ItemId" cssClass="form-control" id="LineID" placeholder="Enter ItemId" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="flightNum">Flight Num:</label>
							<div class="col-sm-4">
								<form:input path="flightNum" cssClass="form-control" id="flightNum" placeholder="Enter To Flight Num" required="required"/>
							</div>
						</div>
						<div class="form-group">
						<label class="control-label col-sm-2" for="flightStd">Flight Std:</label>
							<div class="col-sm-4">
								<form:input type="date" path="flightStd" cssClass="form-control" id="flightStd" placeholder="Enter Flight Std" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="finalSortLocationID">Final Sort Location Id:</label>
							<div class="col-sm-4">
								<form:input path="finalSortLocationID" cssClass="form-control" id="finalSortLocationID" placeholder="Enter To Final Sort Location Id" required="required"/>
							</div>
						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="deviceId">Order Type:</label>
							<div class="col-sm-4">
								<form:select path="orderType" class="form-control" id="orderType" items="${dropdownMenus.orderType}"/>
							</div>
							<label class="control-label col-sm-2" for="orderId">Order Id:</label>
							<div class="col-sm-4">
								<form:input path="orderId" cssClass="form-control" id="orderId" placeholder="Enter Order Id" required="required"/>
							</div>
						</div>
				</form:form>
				</div>

				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
					<button id="command-add-button" type="button" class="btn btn-primary">Add</button>
				</div>

			</div>

		</div>

	</div>

