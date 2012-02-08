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
    	<c:choose>
    	<c:when test="${loginError}">
		<div class="alert alert-error">
		  <strong>!!</strong> Sign in failed, Please check your user and password.
		</div>
    	</c:when>
    	<c:otherwise>
		<div class="alert">
		  <strong>Warning!</strong> Please login.
		</div>
    	</c:otherwise>
    	</c:choose>
      <div class="row">
	      <div class="span8 offset2">
			<form class="form-horizontal" action="<c:url value="/login"/>" method="post">
		        <fieldset>
		          <legend>Sign in Form</legend>
		          <div class="control-group">
		            <label for="input01" class="control-label">User</label>
		            <div class="controls">
		              <input type="text" id="user" name="user" class="input-xlarge">
		            </div>
		          </div>
		          <div class="control-group">
		            <label for="input01" class="control-label">Pass</label>
		            <div class="controls">
		              <input type="password" id="pass" name="pass" class="input-xlarge">
		            </div>
		          </div>
		          <div class="form-actions">
		            <button class="btn btn-primary" type="submit">Sign in</button>
		            <button class="btn" type="reset">Cancel</button>
		          </div>
		        </fieldset>
		      </form>
	      </div>
      </div>
    </div> <!-- /container -->
</body>
</html>