<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<title>SUPPORT | Airflow WCS</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
	<meta name="description" content="WRXJ - Purchase Order / Expected Receipts Screen"/>
	<meta charset="utf-8"/>

	<%-- Static Include --%>
	<%@include file="../_template/header.jspf"%>
	<%@include file="../_template/core_scripts.jspf"%>
	
	<link type="text/css" href="<spring:url value="/resources/css/support.css"/>" rel="stylesheet" />
</head>

<%@include file="../_template/navBodyWrapper.jspf" %>

<div id="supportPanel" class="container-fluid inner-content">

<div class="col-sm-12" >
	<span id="pageInformaton">
	    There are three ways to reach Daifuku support:
	</span>
</div>
<div class="col-sm-12">

<div id="supportOptionsSection">
    <div id="onlineSupport" class="supportSection">
        <div class="supportIcon"></div>

        <span class="supportTitle">Online Support*</span>

        <p class="supportDescription">
            For typical, non-emergency issues please use the Daifuku Online Help form at:
        </p>

        <div class="supportWayToContact"><a href="http://clientportal.wynright.com/">http://clientportal.wynright.com/</a></div>

        <p class="supportDetails">
            Daifuku Online Support is only available to customers with an active user account.
            <br /><br />
            User accounts are provided to those that are within their 90 day warranty period or maintain a Daifuku support contract.
        </p>
    </div>

    <div id="emailSupport" class="supportSection">
        <div class="supportIcon"></div>

        <span class="supportTitle">Email Support*</span>

        <p class="supportDescription">
            For more significant issues or to report problems with the Wynright / Wynsoft website please email support at:
        </p>

        <div class="supportWayToContact"><a href="mailto:client.support@wynright.com">client.support@wynright.com</a></div>

        <p class="supportDetails">
            It is recommended that when reporting issues with the website, a screen shot should be attached to the email whenever possible along with a description
        </p>
    </div>

    <div id="phoneSupport" class="supportSection">
        <div class="supportIcon"></div>

        <span class="supportTitle">Phone Support</span>

        <p class="supportDescription">
            To report an emergency situation or to request immediate assistance with a high priority issue, contact Wynright Support at:
        </p>

        <div class="supportWayToContact">1-888-996-0099</div>

        <p class="supportDetails">
            Wynright phone support is available 24/7 making it the ideal method to report critical issues after standard business hours.
        </p>
    </div>
</div>

		<div class="col-sm-12" >
			<div id="supportDisclaimer">
			    * Emails and online form requests received after 5:00 P.M E.S.T. will be answered the next business day. Phone support is <u>always</u> available.
			</div>
		</div>
	</div>
</div>


<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf" %>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf" %>
	</c:otherwise>
</c:choose>

</html>
