<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
<head>
    <link rel="shortcut icon" href="images/favicon.ico">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Data Quality Reports</title>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/fonts.css">
	<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
	<body>	
	
	<% 
		String getUser=null;
		if(session.getAttribute("user")!=null)
		{
			request.getRequestDispatcher("/WEB-INF/pages/sections/welcome.jsp").forward(request, response);
		}
	
	%>
	
	<nav class="navbar navbar-default" role="navigation">
        <div class="container">
            <!-- Brand and toggle get grouped for better mobile display -->
            <div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-ex1-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <span class="navbar-brand"><span class="fa fa-line-chart" id="page-icon"></span>&nbsp;<span id="page-title">Data Quality Reports</span></span>
            </div>
        
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse navbar-ex1-collapse">
            </div>
        </div>
    </nav>
	<div class="container">
		<div class="row">
			<div class="col-sm-6 col-sm-offset-3">
			<br><br>
				<div class="panel panel-default">
					<div class="panel-body">
						<br>
						<form name='loginForm' action="<c:url value='login' />" method='POST' class="form-horizontal">
							<div class="form-group">
								<input type='hidden' class="form-control" name='j_organization'>
							</div>
							<div class="form-group">
								<label for="username" class="col-sm-4 control-label">User Name:</label>
								<div class="col-sm-6">
									<input type='text' id="username" class="form-control" name='j_username'>
								</div>
							</div>
							<div class="form-group">
								<label for="password" class="col-sm-4 control-label">Password:</label>
								<div class="col-sm-6">
									<input type='password' id="password" class="form-control" name='j_password' />
								</div>
							</div>
							<div class="form-group text-center">
								<input name="submit" type="submit" class="btn btn-default" value="Log In" />
							</div>
						</form>
					</div>
				</div>
				<c:if test="${not empty errorMessage}">
					<div class="alert alert-danger" role="alert">
						<p>${requestScope.errorMessage}</p>
					</div>
				</c:if>
			</div>
		</div>
	</div>
</body>
</html>
