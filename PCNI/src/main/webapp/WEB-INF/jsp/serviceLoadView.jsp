<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${decorator eq 'empty'}">
    <script type="text/javascript">
    	window.DashboardGlobals.folderpath = "${dir}";
    	window.DashboardGlobals.chartData = "${chartData}";
       
    </script>
    ${templateData}
</c:if>

<c:if test="${empty decorator}">
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="decorator" content="minimal" />
        <link rel="shortcut icon" href="images/favicon.ico">
        <title>Data Quality Reports</title>
        <link rel="stylesheet" href="css/styles.css">
        <script>
            var baseUrl = "${pageContext.request.contextPath}/";
        </script>
        <script>
            if(!window.DashboardGlobals)
            {
                window.DashboardGlobals = (function () {
                    
                    var object = {
                        baseUrl : baseUrl,
                        solutionLoader: baseUrl + "getSolutionResources.html",
                        resourceLoader : baseUrl + "getEFWSolution.html",
                        updateService : baseUrl + "executeDatasource.html",
                        chartingService : baseUrl + "visualizeData.html",
                        exportData : baseUrl + "exportData.html",
                        reportDownload : baseUrl + "downloadReport.html",
                        productInfo : baseUrl + "getProductInformation.html",
                        sendMail : baseUrl + "sendMail.html",
                        saveReport : baseUrl + "saveReport.html",
                        executeSavedReport : baseUrl + "executeSavedReport.html",
                        updateEFWTemplate: baseUrl + "updateEFWTemplate.html",
                        /* sessionUserName: "${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.username}",
                        sessionUserEmail: "${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.emailAddress}",
                        sessionUserOrganization : "${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.org_name}" */
                    };
                    
                    return object;
                })();
                window.DashboardGlobals.folderpath = "${dir}";
                
            }
        </script>
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

        <script src="js/dashboard.js"></script>
	<script type="text/javascript" src="js/app.js"></script>
    </head>
    <body>
        <div id="dashboardCanvas">
            <div class="container-fluid" id="main">
					${templateData}
            </div>
        </div>
    </body>
</html>
</c:if>
