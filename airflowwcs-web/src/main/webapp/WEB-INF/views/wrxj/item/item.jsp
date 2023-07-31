<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>ITEM MASTER - Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>
<%@include file="../_template/executing.jspf" %>

<div class="container-fluid">
	<div class="row">
		<spring:url value="/item/listSearch" var="formUrl"/>
		<form:form cssClass="form-horizontal" id="item-filtering-form" action="${formUrl}" method="POST" modelAttribute="itemMasterDataModel" >
			<div class="panel panel-default">
				<%@include file="../_template/searchHeader.jspf" %>
				<div id="collapseCriteria" class="panel-collapse collapse in">
					<div class="panel-body">
						<div class="row">
							<%--------------------------------------------------------%>
							<%-------First Panel: Location Info-----------------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-6" >
								<div class="panel panel-default">
									<div class="row panel-body">
										<%---------Item Fields-------------%>
										<div class="col-sm-5">
											<label for="item" class="control-label">Item ID</label>
											<form:input path="item" cssClass="form-control" id="item" placeholder="Item ID"/>
										</div>
										<div class="col-sm-7">
											<label for="description" class="control-label">Item Description</label>
											<form:input path="description" cssClass="form-control" id="description" placeholder="Description"/>
										</div>
									</div>
								</div>
							</div>
							<%--------------------------------------------------------%>
							<%-------------Second Panel: Search Button----------------%>
							<%--------------------------------------------------------%>
							<div class="col-sm-1">
								<div class="panel noborder">
									<div class="panel-body">
										<div class="form-group-sm">
											<%---------Search Button-------------%>
											<br/>
											<center>
												<button id="searchItemsButton" type="button" class="btn btn-primary"><i class="fa fa-search"></i> Search</button>
											</center>
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

	<security:authorize access="hasRole('ROLE_ADMIN')">
		<wrxj:ajaxTable metaDataName="ItemMaster" ajaxUri="/airflowwcs/table/empty"
				metaId="Item ID" hasRefresh="true" hasFilter="true"
				hasAutoRefresh="true" hasExcel="true" hasColVis="true"  numPageLength="100"></wrxj:ajaxTable>
	</security:authorize>
	
	<security:authorize access="hasRole('ROLE_USER') and !hasRole('ROLE_ADMIN')">
	<wrxj:ajaxTable metaDataName="ItemMaster" ajaxUri="/airflowwcs/table/empty"
				metaId="Item ID" hasRefresh="true" hasFilter="true"  numPageLength="100"></wrxj:ajaxTable>
	</security:authorize>
	


<%@include file="modifyMinMax.jspf"%>
<%@include file="modifyToteQty.jspf"%>

<script src="<spring:url value="/resources/js/item.js"/>" type="text/javascript"></script>
<%@include file="../_template/alertsFloating.jspf"%>
<%-- HAS ADMIN ROLE, LOAD ADMIN SPECIFIC JAVASCRIPT --%>
<security:authorize access="hasRole('ROLE_ADMIN')">
<script type="text/javascript">
	isAdmin=true;
</script>
</security:authorize>

<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>