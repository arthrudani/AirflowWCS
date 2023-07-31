<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>USER PERMISSIONS | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - User Permissions"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	<%@include file="../_template/table_scripts.jspf"%>
	
	<style>
		.panel-heading h3 {
			margin: 0px;
		}
		.centered {
			text-align: center;
		}
	</style>
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>

<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
	<div class="standard-contain">
		<div class="row">
			<div class="col-sm-7">
				<div class="content">
					<div class="panel panel-default">
						<div class="panel-heading">
							<center>
								<i class="fa fa-users fa-2x pull-left" aria-hidden="true"> </i>
								<h3>Permission Groups</h3>
							</center>
						</div>
						<div class="panel-body">
							<wrxj:hibernateAjaxTable
								ajaxUri="/airflowwcs/userpermission/list" inModal="true"
								tableId="UserPermissionGroup" hasRefresh="true"
								entity="com.daifukuamerica.wrxj.web.model.hibernate.AuthGroup"
								hasColVis="true"
								prefHideColumns="${userPref.tableColumnVisibility['UserPermissions']}"
								metaId="Group Name"></wrxj:hibernateAjaxTable>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-5">
				<div class="row">
					<div class="col-sm-12">
						<div class="sidebar-bottom">
							<div class="content">
								<div class="panel panel-default">
									<div class="panel-heading">
										<center>
											<i class="fa fa-drivers-license-o fa-2x pull-left"
												aria-hidden="true"> </i>
											<h3>Details</h3>
										</center>
									</div>
									<div class="panel-body">
										<%@include file="userpermission-detail-roleadmin-card.jspf"%>
										<%@include file="userpermission-detail-roleelevated-card.jspf"%>
										<%@include file="userpermission-detail-rolemaster-card.jspf"%>
										<%@include file="userpermission-detail-rolereadonly-card.jspf"%>
										<%@include file="userpermission-detail-roleunkown-card.jspf"%>
										<%@include file="userpermission-detail-roleuser-card.jspf"%>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="row">
			<div class="col-sm-12">
				<div class="sidebar-top">
					<div class="content">
						<div class="panel panel-default">
							<div class="panel-heading">
								<center>
									<i class="fa fa-user-circle-o fa-2x pull-left" aria-hidden="true"> </i>
									<h3>Users</h3>
								</center>
							</div>
							<div class="panel-body">
								<div class="row ">
									<ul class="nav nav-tabs">
										<li class="active"><a href="#tab-in-group" data-toggle="tab" id="in-group-tab">In Group</a></li>
										<li class=""><a data-toggle="tab" href="#tab-all-users" id="all-users-tab">All Users</a></li>
									</ul>
									<%  String[] explicitUserCols = {"User"}; %>
									<%  String[] groupUserCols = {"User","Granted Access"}; %>
									<div class="tab-content">
										<div class="tab-pane fade in active" id="tab-in-group">
											<wrxj:hibernateAjaxTable
												ajaxUri="/airflowwcs/userpermission/users/empty" 
												tableId="UsersInGroup" hasRefresh="true" hasFilter="true" hasAdd="true" hasDelete="true"
												hasColVis="true" explicitColumns="<%=explicitUserCols%>"
												prefHideColumns="${userPref.tableColumnVisibility['UsersInGroup']}"
												metaId="User" inModal="true"/>
										</div> 
										<div class="tab-pane fade" id="tab-all-users">
											<wrxj:hibernateAjaxTable
												ajaxUri="/airflowwcs/userpermission/users/group/all" 
												tableId="AllUsers" hasRefresh="true" hasFilter="true"
												hasColVis="true" explicitColumns="<%=groupUserCols%>"
												prefHideColumns="${userPref.tableColumnVisibility['AllUsersPermission']}"
												metaId="User" inModal="true" />
										</div> 
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

<%@include file="addUserToGroupPopover.jspf" %>
<%@include file="removeUserFromGroupPopover.jspf" %>

	<script src="<spring:url value="/resources/js/userPermission.js"/>" type="text/javascript"></script>
</security:authorize>
<%@include file="../_template/alertsFloatingHeader.jspf" %>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
</html>