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
	  						<i class="icon-user icon-white"></i> <c:out value="${LOGIN_USER.username}"></c:out> <span class="caret"></span>
	  					</a>
	  					<ul class="dropdown-menu">
	  						<li><a href="<c:url value="/signout"></c:url>">Sign out</a></li>
	  					</ul>
	  				</div>
				</div>
			</c:if>
    	</div>
      <h1>Jabberer bot</h1>
      	<p>Simple java jabber bot.</p>
      <div class="row">
	      <div class="span12">
			  <table class="table table-bordered">
			  	<thead>
			  		<tr>
			  			<th>Robot</th><th>Start Time</th><th>Administrators</th><th>Send offline</th><th>Status</th><th>#</th>
			  		</tr>
			  	</thead>
			  	<tbody>
			  		<c:forEach var="robot" items="${robots}">
			  		<tr>
			  			<td><c:out value="${robot.name}"/>(<c:out value="${robot.username}"/>)</td>
			  			<td><fmt:formatDate value="${robot.startTime}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
			  			<td><c:out value="${robot.administratorNames}" /></td>
			  			<td><c:choose><c:when test="${robot.sendOfflineMessage}"><span class="label label-info">true</span></c:when><c:otherwise><span class="label">false</span></c:otherwise></c:choose></td>
			  			<td>
			  				<div class="btn-group">
			  					<a class="btn <c:choose><c:when test="${robot.status == 'Offline'}">btn-warning</c:when><c:when test="${robot.status == 'LoginFailed'}">btn-danger</c:when><c:otherwise>btn-success</c:otherwise></c:choose> dropdown-toggle" href="#" data-toggle="dropdown">
			  						<i class="icon-leaf icon-white"></i> <c:out value="${robot.status}"></c:out> <span class="caret"></span>
			  					</a>
			  					<ul class="dropdown-menu">
			  						<li><a href="<c:url value="/robot/disconnect"><c:param name="robot" value="${robot.name}" /> </c:url>">Disconnect</a></li>
			  						<li><a href="<c:url value="/robot/reconnect"><c:param name="robot" value="${robot.name}" /> </c:url>">Reconnect</a></li>
			  					</ul>
			  				</div>
			  			</td>
                        <td>
                            <a href="javascript:void(0)" onclick="javascript:showEntryModal('<c:out value="${robot.name}"/>');return false;" class="btn btn-primary"><i class="icon-plus-sign icon-white"></i> Add entry</a>
                        </td>
					</tr>
                    <tr>
                        <td colspan="6">
                        <c:forEach var="entry" items="${robot.rosters}">
                            <c:set var="contains" value="false" />
                            <c:forEach var="item" items="${robot.onlineRosters}">
                                <c:if test="${item.user eq entry.user}">
                                    <c:set var="contains" value="true" />
                                </c:if>
                            </c:forEach>
                            <div class="btn-group">
                                <a class="btn<c:choose><c:when test="${contains}"> btn-success</c:when><c:otherwise> btn-inverse</c:otherwise></c:choose> dropdown-toggle" href="#" data-toggle="dropdown"><i class="icon-user icon-white"></i> <c:out value="${entry.name}"/> <span class="caret"></span></a>
                                <ul class="dropdown-menu">
                                    <li><a href="javascript:void(0)" onclick="javascript:showRenameModal('<c:out value="${robot.name}"/>','<c:out value="${entry.user}"/>','<c:out value="${entry.name}"/>');return false;"><i class="icon-pencil"></i> Rename (<c:out value="${entry.user}"/>)</a></li>
                                    <li><a href="<c:url value="/entry/remove"/>?robotName=<c:out value="${robot.name}"/>&entry=<c:out value="${entry.user}"/>" onclick="return window.confirm('Really delete this entry?')"><i class="icon-trash"></i> Delete (<c:out value="${entry.user}"/>)</a></li>
                                </ul>
                            </div>
                        </c:forEach> &nbsp;
                        </td>
                    </tr>
			  		</c:forEach>
			  	</tbody>
			  </table>
              <div id="entry-modal" class="modal hide fade">
                  <form action="<c:url value="/entry/add"/>" class="form-horizontal">
                      <div class="modal-header">
                          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                          <h3>Add entry into robot</h3>
                      </div>
                      <div class="modal-body">
                          <input type="hidden" name="robotName" id="robotName"/>
                          <div class="control-group">
                              <label class="control-label" for="entry">Entry</label>
                              <div class="controls">
                                  <input type="text" id="entry" name="entry" placeholder="bob@example.com">
                              </div>
                          </div>
                          <div class="control-group">
                              <label class="control-label" for="nickname">Nickname</label>
                              <div class="controls">
                                  <input type="text" id="nickname" name="nickname">
                              </div>
                          </div>
                      </div>
                      <div class="modal-footer">
                          <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                          <button type="submit" class="btn btn-primary">Add</button>
                      </div>
                  </form>
              </div>
              <div id="entry-rename-modal" class="modal hide fade">
                  <form action="<c:url value="/entry/rename"/>" class="form-horizontal">
                      <div class="modal-header">
                          <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                          <h3 id="rename-header">Rename entry</h3>
                      </div>
                      <div class="modal-body">
                          <input type="hidden" name="robotName" id="rename-robotName"/>
                          <input type="hidden" name="entry" id="rename-entry"/>
                          <div class="control-group">
                              <label class="control-label" for="nickname">New Name</label>
                              <div class="controls">
                                  <input type="text" id="newname" name="newname">
                              </div>
                          </div>
                      </div>
                      <div class="modal-footer">
                          <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
                          <button type="submit" class="btn btn-primary">Rename</button>
                      </div>
                  </form>
              </div>
	      </div>
      </div>
    </div> <!-- /container -->
    <script type="text/javascript">
        function showEntryModal(robotName) {
            $('#entry-modal').modal();
            $('#robotName').val(robotName);
        }
        function showRenameModal(robotName, entry, name) {
            $('#entry-rename-modal').modal();
            $('#rename-robotName').val(robotName);
            $('#rename-entry').val(entry);
            $('#newname').attr('placeholder', name);
            $('#rename-header').html('Rename entry [' + entry +']');
        }
    </script>
</body>
</html>