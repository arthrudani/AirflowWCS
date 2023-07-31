<!-- LOAD ADD  -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!-- LOAD ADD -->
	<div id="add-modal" class="modal fade">

		<div class="modal-dialog modal-lg">

			<div class="modal-content">

				<div class="modal-header">

					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>

					<h4 class="modal-title">Add Load</h4>

				</div>

				<div class="modal-body" id="addModalBody">
				
				<!--  body here inserted by javascript calling server to bet the rest... -->
				<spring:url value="/load/add" var="formUrl"/>
				<form:form cssClass="form-horizontal" id="load-add-form" action="${formUrl}" method="POST" modelAttribute="loadDataModel">

						<div class="form-group">
							<label class="control-label col-sm-2" for="loadid">Load ID:</label>
							<div class="col-sm-4">
								<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Enter Load ID" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="containerType">Container:</label>
							<div class="col-sm-4">
								<form:select path="containerType" cssClass="form-control" id="containerType" required="required" items="${dropdownMenus.containerTypes}"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="weight">Weight:</label>
							<div class="col-sm-4">
								<form:input path="weight" cssClass="form-control" id="weight" value="0.0"/>
							</div>
							<label class="control-label col-sm-2" for="height">Height:</label>
							<div class="col-sm-4">
								<form:select path="height" cssClass="form-control" id="height" items="${dropdownMenus.heights}"/>
							</div>
						</div>

						<div class="form-group" id="locationContainer">
							<label class="control-label col-sm-2" for="locWarehouse">Location:</label>
							<div class="col-sm-4">
								<form:select path="warehouse" cssClass="form-control col-sm-2" id="warehouse">
									    <form:option value=""></form:option>
     									<form:options items="${dropdownMenus.warehouses}" />
								</form:select>
							</div>
							<div class="col-sm-4">
								<form:input path="address" cssClass="form-control col-sm-4" id="address" placeholder="Address"/>
							</div>
						</div>

						<div class="form-group" id="nextLocationContainer">
						<label class="control-label col-sm-2">Next Location:</label>
							<div class="col-sm-4">
								<form:select path="nextWarehouse" cssClass="form-control col-sm-2" id="nextWarehouse">
										<form:option value=""></form:option>
     									<form:options items="${dropdownMenus.warehouses}" />
								</form:select>
								
							</div>
							<div class="col-sm-4">
								<form:input path="nextAddress" cssClass="form-control col-sm-4" id="nextAddress" placeholder="Next Address"/>
							</div>
						</div>

						<div class="form-group">
							<label class="control-label col-sm-2" for="finalWarehouse">Final Location:</label>
							<div class="col-sm-4">
								<form:select path="finalWarehouse" cssClass="form-control col-sm-2" id="finalWarehouse">
										<form:option value=""></form:option>
     									<form:options items="${dropdownMenus.warehouses}" />
								</form:select>
							</div>
							<div class="col-sm-4">
								<form:input path="finalAddress" cssClass="form-control col-sm-4" id="finalAddress" placeholder="Final Address"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="routeId">Route:</label>
							<div class="col-sm-2">
								<form:select path="routeId" cssClass="form-control col-sm-2" id="routeId" items="${dropdownMenus.routeList}">
										<form:option value=""></form:option>
     									<form:options items="${dropdownMenus.routeList}" />
								</form:select>
							</div>
							<label class="control-label col-sm-2" for="moveStatus">Move Status:</label>
							<div class="col-sm-4">
								<form:select path="moveStatus" cssClass="form-control" id="moveStatus" items="${dropdownMenus.moveStatuses}"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="message">Message:</label>
							<div class="col-sm-10">
								<form:input path="message" cssClass="form-control" id="message" placeholder="Enter Message"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="barcode">Barcode:</label>
							<div class="col-sm-10">
								<form:input path="barcode" cssClass="form-control" id="barcode" placeholder="Enter barcode"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="amountFull">Amount Full:</label>
							<div class="col-sm-4">
								<form:select path="sAmountFull" cssClass="form-control" id="amountFull" required="required" items="${dropdownMenus.amountFull}"/>
							</div>
							<label class="control-label col-sm-2" for="lpCheck">LP Check:</label>
							<div class="col-sm-2">
								<form:select path="lpCheck" cssClass="form-control" id="amountFull" required="required" items="${dropdownMenus.lpCheck}"/>
							</div>
						</div>
						
						<div class="form-group">

						</div>
						<div class="form-group">
							<label class="control-label col-sm-2" for="deviceId">Device ID:</label>
							<div class="col-sm-4">
								<form:select path="deviceId" class="form-control" id="deviceId" items="${dropdownMenus.deviceList}"/>
							</div>
							<label class="control-label col-sm-2" for="recZone">Recommend Zone:</label>
							<div class="col-sm-2">
								  <select name="recZone" class="form-control" id="recZone">
							        <option></option>
							      </select>
							</div>
						</div>
				
				</form:form>

				
				

				</div>

				<div class="modal-footer">

					<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>

					<button id="load-add-button" type="button" class="btn btn-primary">Add</button>

				</div>

			</div>

		</div>

	</div>

<!-- END LOAD ADD -->