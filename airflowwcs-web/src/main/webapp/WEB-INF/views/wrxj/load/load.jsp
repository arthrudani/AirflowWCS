<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose>
	<c:when test="${wrxHasInventory==true}">
		<%@include file="load-inventory.jsp" %>
	</c:when>
	<c:when test="${wrxHasShelf==true}">
		<%@include file="load-loadmover-shelf.jsp" %>
	</c:when>
	<c:otherwise>
		<%@include file="load-loadmover.jsp" %>
	</c:otherwise>
</c:choose>
