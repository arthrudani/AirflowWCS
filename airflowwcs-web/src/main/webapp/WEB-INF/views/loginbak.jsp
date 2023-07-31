
<%-- 
    Document   : Login - Wrx
    Created on : 3/31/17
    Author     : Dylan Stout
    
    Default login authentication page for wrx web client
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="Login to Airflow WCS">

<!-- Static Include -->
<%@include file="wrxj/_template/header.jspf"%>

</head>

<body class="login-body">
	<div class="container">
		<div class="row">
			<div class="col-sm-6 col-md-4 col-md-offset-4">
				<div class="login-card">
					<img class="login-logo"
						src="<spring:url 
						value="/resources/img/wrx_lg_logo.png"/>"
						alt="WRXJ User Login"
					> 
					<img class="profile-img"
						 src="<spring:url value="/resources/img/nouser.png"/>" 
						 alt=""
					>
					<form id="loginForm" method="post" class="form-signin"
						action="${pageContext.request.contextPath}/login">
						<input id="userName" name="username" type="text"
							class="form-control" placeholder="User" value='${login.username}'
							required autofocus>
						 <input type="password" id="password"
							name="password" value='' class="form-control"
							placeholder="Password" required>
						<button class="btn btn-lg btn-primary btn-block" type="submit">
							Login</button>
						${login.loginError}

					</form>
				</div>
			</div>
		</div>
	</div>
</body>
</html>