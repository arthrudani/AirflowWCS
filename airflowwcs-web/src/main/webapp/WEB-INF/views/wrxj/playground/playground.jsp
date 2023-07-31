<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
        
<!-- Tag Libraries -->
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="wrxj" uri="wrxj-taglib" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="WRXJ - Load Screen">
<title>DEV PLAYGROUND | Airflow WCS</title>

<!-- Static Include -->
<%@include file="../_template/header.jspf"%>
<%@include file="../_template/core_scripts.jspf"%>
<%@include file="../_template/table_scripts.jspf"%>
<%@include file="../_template/chart_scripts.jspf"%>

</head>
<%@include file="../_template/navBodyWrapper.jspf" %>

<%@include file="../_template/alerts.jspf" %>
<security:authorize access="hasAnyRole('ROLE_MASTER')">
<script src="//d3js.org/d3.v3.min.js"></script>
<script src="//d3js.org/topojson.v1.min.js"></script>
<script>

var width = 960,
    height = 500;

var projection = d3.geo.albersUsa()
    .scale(1070)
    .translate([width / 2, height / 2]);

var path = d3.geo.path()
    .projection(projection);

var zoom = d3.behavior.zoom()
    .translate(projection.translate())
    .scale(projection.scale())
    .scaleExtent([height, 8 * height])
    .on("zoom", zoomed);

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

var g = svg.append("g")
    .call(zoom);

g.append("rect")
    .attr("class", "background")
    .attr("width", width)
    .attr("height", height);

d3.json("/airflowwcs/playground/map.json", function(error, us) { 
  if (error) throw error;

  g.append("g")
      .attr("id", "states")
    .selectAll("path")
      .data(topojson.feature(us, us.objects.states).features)
    .enter().append("path")
      .attr("d", path)
      .on("click", clicked);

  g.append("path")
      .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a !== b; }))
      .attr("id", "state-borders")
      .attr("d", path);
});

function clicked(d) {
  var centroid = path.centroid(d),
      translate = projection.translate();

  projection.translate([
    translate[0] - centroid[0] + width / 2,
    translate[1] - centroid[1] + height / 2
  ]);

  zoom.translate(projection.translate());

  g.selectAll("path").transition()
      .duration(700)
      .attr("d", path);
}

function zoomed() {
  projection.translate(d3.event.translate).scale(d3.event.scale);
  g.selectAll("path").attr("d", path);
}

</script>
<div class="panel panel-default">
  <div class="panel-body">
  		<div class="graph-area" id="graph"><svg width="960" height="600"></svg></div>
  </div>
</div>

    
  
</security:authorize>
<%@include file="../_template/navBodyWrapperEnd.jspf" %>
<script src="<spring:url value="/resources/js/playground.js"/>"
	type="text/javascript"></script>
<!-- HAS ADMIN ROLE, ROLE ADMIN SPECIFIC JAVASCRIPT -->
<security:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MASTER')">
 <script type="text/javascript">
 	isAdmin=true;
 </script>
</security:authorize>
</html>