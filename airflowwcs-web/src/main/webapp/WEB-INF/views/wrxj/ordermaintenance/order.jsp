<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose>
	<c:when test="${wrxHasInventory==true}">
		<%@include file="order-inventory.jsp" %>
	</c:when>
	<c:otherwise>
		<%@include file="order-loadmover.jsp" %>
	</c:otherwise>
</c:choose>
