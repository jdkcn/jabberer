<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%@ 
taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %><!DOCTYPE html>
<html lang="en">
<head>
	<%@include file="header.jsp" %>
</head>
<body>
	<%@include file="nav.jsp" %>
    <div class="container">
    	<div class="row">
    		<div class="span10">
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
    		</div>
    		<c:if test="${LOGIN_USER ne null}">
				<div class="span2">
					
					<div class="btn-group">
	  					<a class="btn btn-info dropdown-toggle" href="#" data-toggle="dropdown">
	  						<i class="icon-user icon-white"></i> <c:out value="${LOGIN_USER}"></c:out> <span class="caret"></span>
	  					</a>
	  					<ul class="dropdown-menu">
	  						<li><a href="<c:url value="/signout"></c:url>">Sign out</a></li>
	  					</ul>
	  				</div>
				</div>
			</c:if>
    	</div>
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