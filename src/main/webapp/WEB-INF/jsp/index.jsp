<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8" />
	<title>Hello Jabberer</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="Rory Ye">
    <!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
    <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->
	<!-- Le styles -->
    <link href="<c:url value="/assets/css/bootstrap.css"/>" rel="stylesheet">
    <link href="<c:url value="/assets/css/bootstrap-responsive.css"/>" rel="stylesheet">
    <style>
      body {
        padding-top: 60px;
      }
    </style>
	<script src="<c:url value="/assets/js/jquery-1.7.1.min.js"/>"></script>
	<script src="<c:url value="/assets/js/bootstrap.min.js"/>"></script>
</head>
<body>
	<div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
          <a class="brand" href="#">Jabberer</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class="active"><a href="#">Home</a></li>
              <li><a href="#about">About</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div class="container">
    	<c:choose>
    	<c:when test="${allRobotsOnline}">
		<div class="alert alert-success">
		  <strong>Congratulations!</strong> Every robot online.
		</div>
    	</c:when>
    	<c:otherwise>
		<div class="alert">
		  <strong>Warning!</strong> Some robot not online.
		</div>
    	</c:otherwise>
    	</c:choose>
      <h1>Jabberer gtalk bot</h1>
      	<p>Simple java gtalk bot.</p>
      <div class="row">
	      <div class="span12">
			  <table class="table table-bordered">
			  	<thead>
			  		<tr>
			  			<th>Robot</th><th>Start Time</th><th>Online Users</th><th>Administrators</th><th>Send offline</th><th>Status</th>
			  		</tr>
			  	</thead>
			  	<tbody>
			  		<c:forEach var="robot" items="${robots}">
			  		<tr>
			  			<td><c:out value="${robot.name}"/></td>
			  			<td><fmt:formatDate value="${robot.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
			  			<td><c:out value="${robot.onlineRosterNames}" /></td>
			  			<td><c:out value="${robot.administratorNames}" /></td>
			  			<td><c:choose><c:when test="${robot.sendOfflineMessage}"><span class="label label-info">true</span></c:when><c:otherwise><span class="label">false</span></c:otherwise></c:choose></td>
			  			<td>
			  				<div class="btn-group">
			  					<a class="btn <c:choose><c:when test="${robot.status == 'Offline'}">btn-danger</c:when><c:otherwise>btn-success</c:otherwise></c:choose> dropdown-toggle" href="#" data-toggle="dropdown">
			  						<i class="icon-leaf icon-white"></i> <c:out value="${robot.status}"></c:out> <span class="caret"></span>
			  					</a>
			  					<ul class="dropdown-menu">
			  						<li><a href="<c:url value="/robot/disconnect"><c:param name="robot" value="${robot.name}" /> </c:url>">Disconnect</a></li>
			  						<li><a href="<c:url value="/robot/reconnect"><c:param name="robot" value="${robot.name}" /> </c:url>">Reconnect</a></li>
			  					</ul>
			  				</div>
			  			</td>
					</tr>
			  		</c:forEach>
			  	</tbody>
			  </table>
	      </div>
      </div>
    </div> <!-- /container -->
</body>
</html>