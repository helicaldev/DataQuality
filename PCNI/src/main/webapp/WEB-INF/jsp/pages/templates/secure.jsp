<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- <auth:authorize access="hasRole('ROLE_USER')">
	<tiles:insertAttribute name="banner-content" />
	<tiles:insertAttribute name="body-content" />
	<tiles:insertAttribute name="footer-content" />
</auth:authorize> --%>
 
<auth:authorize access="hasRole('ROLE_ADMIN')">

<!DOCTYPE html>
<html>
<head>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
	<script src="${pageContext.request.contextPath}/js/libs/jquery.js"></script>
	<script src="${pageContext.request.contextPath}/js/libs/bootstrap.js"></script>
	<style>
		html, body{
			height:100%;
		}
		.navbar{
			margin-bottom: 0;
		}
		.navmenu{
			background: none;
		}
		.full-body{
			min-height:100%;
		}
	</style>
</head>
	<body>
	<nav class="navbar navbar-inverse" role="navigation">
	  <div class="container">
	    <!-- Brand and toggle get grouped for better mobile display -->
	    <div class="navbar-header">
	      <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
	        <span class="sr-only">Toggle navigation</span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	        <span class="icon-bar"></span>
	      </button>
	      <a class="navbar-brand" href="#">HDI</a>
	    </div>
	
	    <!-- Collect the nav links, forms, and other content for toggling -->
	    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
		  <ul class="nav navbar-nav navbar-right">
	        <li><a href="#">Welcome Admin</a></li>
	        <li><a href="<c:url value="/j_spring_security_logout" />"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li>
	      </ul>
	    </div>
	  </div>
	</nav>
		<tiles:insertAttribute name="banner-content" />
		<div class="full-body container">
			<div class="row">
			  <div class="col-sm-3">
			  	<tiles:insertAttribute name="menu-content" />
			  </div>
			  <div class="col-sm-9">
			  		<br><br>
					<tiles:insertAttribute name="body-content" />
				</div>
			</div>
		</div>	
		<tiles:insertAttribute name="footer-content" />
	</body>
</html>
</auth:authorize>