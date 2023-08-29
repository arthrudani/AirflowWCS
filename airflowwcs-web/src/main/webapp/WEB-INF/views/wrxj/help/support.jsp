<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1" trimDirectiveWhitespaces="true"%>
<%-- Tag Libraries --%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="wrxj" uri="wrxj-taglib"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>SUPPORT | Airflow WCS</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<meta name="description"
	content="WRXJ - Purchase Order / Expected Receipts Screen" />
<meta charset="utf-8" />

<%-- Static Include --%>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap@4.4.1/dist/css/bootstrap.min.css"
	integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh"
	crossorigin="anonymous">
<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.5.0/font/bootstrap-icons.css">
<link type="text/css"
	href="<spring:url value="/resources/css/support.css"/>"
	rel="stylesheet" />
</head>

<%@include file="../_template/navBodyWrapper.jspf"%>

<div id="supportPanel" class="container-fluid inner-content">

	<!-- <div class="col-sm-12" > -->
	<!-- 	<span id="pageInformaton"> -->
	<!-- 	    There are three ways to reach Daifuku support: -->
	<!-- 	</span> -->
	<!-- </div> -->
	<div class="col-sm-12">

		<div id="supportOptionsSection">
			<div class="article_header container">
				<h1 class="d-flex justify-content-between">
					<div class="d-flex align-items-center">Customer Services</div>
					<div>
						<a href="https://www.linkedin.com/company/daifuku/"
							class="article_title_icon" target="_blank" rel="noopener"> <i
							class="fa fa-2x fa-linkedin" style="color: #0072b1"></i>
						</a>
					</div>
				</h1>
			</div>

			<img class="img-fluid mt-4"
				src="<spring:url value="/resources/img/service-top-image.png"/>">

			<div class="mt-5 container">Daifuku offers a comprehensive range of
				services to ensure the stable operation of your system. With service
				and parts centres all over the world, our experienced service
				engineers will support you in ensuring the stable operation of
				system.</div>

			<section class="boxContact l-section hide_if_de hide_if_es mb-5 container">
				<h3 class="boxContact_heading">Contact Us</h3>
				<p class="boxContact_text">For product inquiries, please reach
					out to us via our Contact Us page.</p>
				<a class="boxContact_btn c-btn -flex -center -next"
					href="https://inquiry.daifuku.com/products_en"> <span
					class="c-btn_label"><i class="bi-chevron-right"></i>&nbsp;&nbsp;&nbsp;Contact
						Us</span>
				</a>
				<p class="u-mtSmall">
					<a href="https://www.daifuku.com/company/groupcompanies/"
						target="_blank" rel="noopener" class="c-linkArrow"> <i
						class="bi-chevron-right"></i>&nbsp;&nbsp;&nbsp;BY TELEPHONE
					</a>
				</p>
			</section>


			<div class="c-grid u-mbLarge d-flex justify-content-center" data-grid-col="3-2-1">
				<section class="panel c-grid_col"> <a class="panel_link"
					href="https://www.daifuku.com/solution/intralogistics/service-and-support/service-policy/">
					<div class="panel_image">
						<img class="img-fluid" src="<spring:url value="/resources/img/index1-2.png"/>"
							alt="" width="284" height="240" loading="lazy">
					</div>
					<h3 class="panel_title"><i class="bi-chevron-right"></i>&nbsp;&nbsp;Service &amp; Support</h3>
				</a> </section>
				<section class="panel c-grid_col"> <a class="panel_link"
					href="https://www.daifuku.com/solution/intralogistics/service-and-support/preventive/">
					<div class="panel_image">
						<img class="img-fluid" src="<spring:url value="/resources/img/index2-1.png"/>"
							alt="" width="284" height="240" loading="lazy">
					</div>
					<h3 class="panel_title"><i class="bi-chevron-right"></i>&nbsp;&nbsp;Preventive Service</h3>
				</a> </section>
				<section class="panel c-grid_col"> <a class="panel_link"
					href="https://www.daifuku.com/solution/intralogistics/service-and-support/retrofitting/">
					<div class="panel_image">
						<img class="img-fluid" src="<spring:url value="/resources/img/index4-2.png"/>"
							alt="" width="284" height="240" loading="lazy">
					</div>
					<h3 class="panel_title"><i class="bi-chevron-right"></i>&nbsp;&nbsp;Retrofitting</h3>
				</a> </section>
			</div>

			<!--     <div id="onlineSupport" class="supportSection"> -->
			<!--         <div class="supportIcon"></div> -->
			<!--         <span class="supportTitle">Online Support*</span> -->
			<!--         <p class="supportDescription"> -->
			<!--             For typical, non-emergency issues please use the Daifuku Online Help form at: -->
			<!--         </p> -->
			<!--         <div class="supportWayToContact"><a href="http://clientportal.wynright.com/">http://clientportal.wynright.com/</a></div> -->
			<!--         <p class="supportDetails"> -->
			<!--             Daifuku Online Support is only available to customers with an active user account. -->
			<!--             <br /><br /> -->
			<!--             User accounts are provided to those that are within their 90 day warranty period or maintain a Daifuku support contract. -->
			<!--         </p> -->
			<!--     </div> -->

			<!--     <div id="emailSupport" class="supportSection"> -->
			<!--         <div class="supportIcon"></div> -->
			<!--         <span class="supportTitle">Email Support*</span> -->
			<!--         <p class="supportDescription"> -->
			<!--             For more significant issues or to report problems with the Wynright / Wynsoft website please email support at: -->
			<!--         </p> -->
			<!--         <div class="supportWayToContact"><a href="mailto:client.support@wynright.com">client.support@wynright.com</a></div> -->
			<!--         <p class="supportDetails"> -->
			<!--             It is recommended that when reporting issues with the website, a screen shot should be attached to the email whenever possible along with a description -->
			<!--         </p> -->
			<!--     </div> -->

			<!--     <div id="phoneSupport" class="supportSection"> -->
			<!--         <div class="supportIcon"></div> -->
			<!--         <span class="supportTitle">Phone Support</span> -->
			<!--         <p class="supportDescription"> -->
			<!--             To report an emergency situation or to request immediate assistance with a high priority issue, contact Wynright Support at: -->
			<!--         </p> -->
			<!--         <div class="supportWayToContact">1-888-996-0099</div> -->
			<!--         <p class="supportDetails"> -->
			<!--             Wynright phone support is available 24/7 making it the ideal method to report critical issues after standard business hours. -->
			<!--         </p> -->
			<!--     </div> -->

		</div>

		<div class="col-sm-12 d-flex justify-content-center">
			<div id="supportDisclaimer">
				* Emails and online form requests received after 5:00 P.M E.S.T.
				will be answered the next business day. Phone support is <u>always</u>
				available.
			</div>
		</div>
		<div class="js-translate l-footer_translate" style="display: none;">Translated
			by machine</div>
		<div
			class="js-pagetop l-footer_pagetop d-flex align-items-center justify-content-center"
			style="position: absolute; top: 1220px;">
			
<!-- 			<i class="bi bi-caret-up"></i> -->
			<a href="#top">TOP</a>
		</div>
	</div>
</div>


<c:choose>
	<c:when test="${userPref.hasDebug==true}">
		<%@include file="../_template/navBodyWrapperEndMessagePopout.jspf"%>
	</c:when>
	<c:otherwise>
		<%@include file="../_template/navBodyWrapperEnd.jspf"%>
	</c:otherwise>
</c:choose>

</html>
