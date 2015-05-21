<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<head>
   <title>PCNI Admin</title>
    <link rel="shortcut icon" href="images/favicon.ico">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="stylesheet" href="css/fonts.css">
        <link rel="stylesheet" href="css/styles.css">
        <script src="js/libs/jquery.js"></script>
        <script src="js/libs/bootstrap.js"></script>
        <script src="js/libs/backbone.js"></script>
        <script src="js/libs/d3.js"></script>
        <script src="js/plugins/utilities.js"></script>
        <script src="js/plugins/filetree.js"></script>
        <script src="js/plugins/bootbox.js"></script>
        <script src="js/plugins/daterangepicker.js"></script>
        <script src="js/plugins/select2.js"></script>
        <script src="js/plugins/filtertable.js"></script>
        <script src="js/dashboard.js"></script>
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
	        <li><a href="<c:url value="/logout" />"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li>
	      </ul>
	    </div>
	  </div>
	</nav>
		
		<div class="full-body container">
			<div class="row">
			  <div class="col-sm-3">
			  <nav class="navmenu navmenu-default" role="navigation">
			<a class="navmenu-brand" href="#">Admin Panel</a>
			<ul class="nav navmenu-nav">
				 <li><a href="<c:url value='/adminCreateUser.html'/>"><span class="fa fa-user"></span> Users</a></li>
				 <li><a href="<c:url value='/adminHDI.html'/>"><span class="fa fa-user"></span>HDI</a></li>
			</ul>
			</nav>
			  
			  </div>
			  <div class="col-sm-9">
			  		<br><br>
						  
				<%@ include file="/WEB-INF/jsp/pages/secure/admin/createUser.jsp" %>
				
				</div>
				
			</div>
		</div>
</body>		