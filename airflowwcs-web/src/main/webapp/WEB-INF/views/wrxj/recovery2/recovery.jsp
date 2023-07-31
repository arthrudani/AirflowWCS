<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:choose>
	<c:when test="${wrxHasInventory==true}">
		<%@include file="recovery-inventory.jsp" %>
	</c:when>
	<c:when test="${wrxHasShelf==true}">
		<%@include file="recovery-loadmover-shelf.jsp" %>
	</c:when>
	<c:otherwise>
		<%@include file="recovery-loadmover.jsp" %>
	</c:otherwise>
</c:choose>
