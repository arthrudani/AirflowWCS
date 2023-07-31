<%-- 
    Document   : Logout success - ScanUI
    Created on : 7/15/2016
    Author     : Dylan Stout
    
    Default logout success page for ScanUI. 
    
--%>  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Airflow WCS -- ScanUI</title>
<meta name="viewport" content="width=320, height=240, user-scalable=no,
initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0" />

	<!--[if lte IE 7]>
	<link href="${pageContext.request.contextPath}/css/base/scanBase.css" rel="stylesheet">
	<script src="${pageContext.request.contextPath}/scripts/jquery-1.10.2.js"></script>
	<script src="${pageContext.request.contextPath}/scripts/jquery-ui-1.10.4.custom.js"></script>
	<script src="${pageContext.request.contextPath}/scripts/piolax/formController.js"></script>
	<![endif]-->
	<!--[if gte IE 8]>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.theme-1.4.5.min.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.icons-1.4.5.min.css" />
  	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.structure-1.4.5.min.css" /> 
  	<script src="${pageContext.request.contextPath}/scripts/mobile/jquery-1.11.1.min.js"></script> 
 	<script src="${pageContext.request.contextPath}/scripts/mobile/jquery.mobile-1.4.5.min.js"></script> 
 	<script src="${pageContext.request.contextPath}/scripts/piolax/formController.js"></script>
 	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/base/responsive.css" /> 
	<![endif]-->
	<!--[if !IE]> -->
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.theme-1.4.5.min.css" />
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.icons-1.4.5.min.css" />
  	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery/jquery.mobile.structure-1.4.5.min.css" /> 
  	<script src="${pageContext.request.contextPath}/scripts/mobile/jquery-1.11.1.min.js"></script> 
 	<script src="${pageContext.request.contextPath}/scripts/mobile/jquery.mobile-1.4.5.min.js"></script> 
 	<script src="${pageContext.request.contextPath}/scripts/piolax/formController.js"></script>
 	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/base/responsive.css" /> 
	<!-- <![endif]-->
	

</head>
<body onload="onBodyLoad()">
	<div class="middleContent">
			<center>
				<table width="100%" height="100%">
					<tr>
						<th colspan=2>LOGOUT</th>
					</tr>
					
					<tr >
						<td>
							<center>Logout successful! Proceed to login screen to continue using application.</center>
						</td>
					</tr>
					
					<tr>
						<td >
							<form action="${pageContext.request.contextPath}/login.jsp">
    							<center><input type="submit" value="Go to Login Screen" /></center>
							</form>
						</td>
					</tr>
				</table>
			</center>
	</div>
</body>
</html>