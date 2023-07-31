<%-- 
    Document   : 404 - ScanUI
    Created on : 7/15/2016
    Author     : Dylan Stout
    
	404 error page. 
    
--%>  
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page isErrorPage="true" %>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>404 - Scan UI</title>
</head>
<body onload="onBodyLoad()">
	<div class="middleContent">
		<table width="100%" height="100%">
			<tr>
				<th>404- Not Found</th>
			<tr>
				<td>The page you are trying to reach cannot be found. <br> 
				</td>
			</tr>
			<tr>
				<td>
					<form action="${pageContext.request.contextPath}/login.jsp">
						<center>
							<input type="submit" value="Return to Login" />
						</center>
					</form>
				</td>
			</tr>
		</table>
	</div>
</body>
</html>