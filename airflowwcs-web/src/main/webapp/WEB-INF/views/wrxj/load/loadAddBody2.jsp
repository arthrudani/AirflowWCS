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
							<label class="control-label col-sm-2" for="loadId">Load ID:</label>
							<div class="col-sm-4">
								<form:input path="loadId" cssClass="form-control" id="loadId" placeholder="Enter Load ID" required="required"/>
							</div>
							<label class="control-label col-sm-2" for="containerType">Container:</label>
							<div class="col-sm-4">
								<form:select path="containerType" cssClass="form-control" id="containerType" required="required" items="${dropdownMenus.containerTypes}"/>
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