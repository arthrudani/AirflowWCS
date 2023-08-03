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
				
				<!--  body here inserted by java script calling server to bet the rest... -->
				<spring:url value="/load/add" var="formUrl"/>
				<form:form cssClass="form-horizontal" id="load-add-form" action="${formUrl}" method="POST" modelAttribute="loadAndLLIDataModel">

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
							<label class="control-label col-sm-2" for="routeId">Shelf position:</label>
							<div class="col-sm-4">
								<form:input path="shelfPosition" cssClass="form-control" id="shelfPosition" placeholder="Enter shelf position" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="moveStatus">Move Status:</label>
							<div class="col-sm-4">
								<form:select path="moveStatus" cssClass="form-control" id="moveStatus" items="${dropdownMenus.moveStatuses}"/>
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
							<label class="control-label col-sm-2">Flight number:</label>
							<div class="col-sm-4">
								<form:input path="lot" cssClass="form-control col-sm-4" id="lot" placeholder="Flight number:"/>
							</div>
						</div>
						
						<div class="form-group" id="nextLocationContainer">
							<label class="control-label col-sm-2">Flight STD:</label>
							<div class="col-sm-4">
								<form:input type="date" path="expectedDate" cssClass="form-control col-sm-4" id="expectedDate" placeholder="Flight STD:"/>
							</div>
						</div>
						
						<div class="form-group" id="nextLocationContainer">
							<label class="control-label col-sm-2">Flight expiry date:</label>
							<div class="col-sm-4">
								<form:input type="date" path="expirationDate" cssClass="form-control col-sm-4" id="expirationDate" placeholder="Flight expiry date:"/>
							</div>
						</div>
						
						<div class="form-group" id="nextLocationContainer">
							<label class="control-label col-sm-2">Global Id:</label>
							<div class="col-sm-4">
								<form:input path="globalId" cssClass="form-control col-sm-4" id="globalId" placeholder="Global Id"/>
							</div>
						</div>

						<div class="form-group">
							<label class="control-label col-sm-2" for="barcode">Barcode:</label>
							<div class="col-sm-10">
								<form:input path="barcode" cssClass="form-control" id="barcode" placeholder="Enter barcode"/>
							</div>
						</div>
						
						<div class="form-group">
							<label class="control-label col-sm-2" for="message">Message:</label>
							<div class="col-sm-10">
								<form:input path="message" cssClass="form-control" id="message" placeholder="Enter Message"/>
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