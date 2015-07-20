<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
    <title>PCNI</title>
    <link rel="shortcut icon" href="images/favicon.ico">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Data Quality Reports</title>
        <link rel="stylesheet" href="css/fonts.css">
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
                        updateEFWTemplate: baseUrl + "updateEFWTemplate.html",
                        sessionUserName: "${sessionScope.user}",
                        sessionUserRoles: "${sessionScope.Roles}",
                        /*sessionUserEmail: "${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.emailAddress}",
                        sessionUserOrganization : "${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.org_name}", */
                        controllers :{
                            efw : baseUrl + "getEFWSolution.html",
                            efwsr : baseUrl + "executeSavedReport.html",
                            efwfav : baseUrl + "executeFavourite.html"

                        },
                        saveReport : baseUrl + "saveReport.html",
                        fsop: baseUrl + "fileSystemOperations.html",
                        importFile: baseUrl + "importFile.html",
                        downloadEnableSavedReport: baseUrl + "downloadEnableSavedReport.html",
                        savePath: "DataQualityReport/Saved Reports",
                        "scheduling":{
                            "get" : baseUrl + "getScheduleData.html",
                            "update": baseUrl + "updateScheduleData.html"
                        }
                    };

                    return object;
                })();
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
</head>
<body>
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
                <span class="navbar-brand">
                	<span class="fa fa-line-chart" id="page-icon"></span>&nbsp;
                		<span id="page-title">Data Quality report</span>
                		
                </span>
                
            </div>
        
            <!-- Collect the nav links, forms, and other content for toggling -->
            <div class="collapse navbar-collapse navbar-ex1-collapse">
                <ul class="nav navbar-nav navbar-right">
                    <li>
                        <form class="navbar-form navbar-left" role="search">
                            <div class="input-group">
                                <span class="input-group-addon"><span class="fa fa-search"></span></span>
                                <input type="text" class="form-control" placeholder="Search" id="search-tree">
                            </div>
                        </form>
                    </li>
                    <li><a href="#"><span id="showRoles"></span></a></li>
                    <script>
                	$("#showRoles").html(window.DashboardGlobals.sessionUserName + " | " + window.DashboardGlobals.sessionUserRoles);</script>
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown"><span class="fa fa-lg fa-bars"></span></a>
                        <ul class="dropdown-menu">
                            <li><a href="#">Account Settings</a></li>
                            <li><a href="<c:url value="/logout" />">Logout</a></li>
                        </ul>
                    </li>
                </ul>
            </div><!-- /.navbar-collapse -->
        </div>
    </nav>
    <div class="container" id="pcni-sub-menu">
        <div class="row">
            <div class="form-inline pull-left hidden">
                <div class="checkbox">
                    <label>
                        <input type="checkbox" id="selectAll"> Select All
                    </label>
                </div>&nbsp;&nbsp;&nbsp;&nbsp;
                <div class="btn-group">
                    <button class="btn btn-default" data-fsaction="move" title="Move to">
                        <span class="fa fa-folder-open"></span>
                    </button>
                    <button class="btn btn-default" data-fsaction="delete" title="Delete">
                        <span class="fa fa-trash"></span>
                    </button>
                    <button class="btn btn-default" id="refresh-tree" title="Refresh">
                        <span class="fa fa-refresh"></span>
                    </button>
                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                        More <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu" role="menu" id="fs-menu">
                        <li class="disabled-favourites disabled-saved-reports">
                            <a href="#" data-fsaction="export">Export</a>
                        </li>
                        <li class="disabled-favourites disabled-saved-reports">
                            <a href="#" data-fsaction="import">Import</a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" data-fsaction="rename">Rename</a>
                        </li>
                        <li>
                            <a href="#" data-fsaction="new-folder">New folder</a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a href="#" data-fsaction="move">Move to</a>
                        </li>
                        <li>
                            <a href="#" data-fsaction="delete">Delete</a>
                        </li>
                    </ul>
                </div>
            </div>
            <a href="./" class="hidden hidden-print"><span class="fa fa-home fa-2x pull-right home-link"></span></a>
        </div>
        <br>
    </div>
    <div class="container">
        <div class="row" id="main"></div>
    </div>
    
    <!-- Modal for Emailing -->
    <div class="modal" id="emailing-modal" data-backdrop="static">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only">Close</span>
                    </button>
                    <h4 class="modal-title">
                        <b>
                            <i>Report Emailing</i>
                        </b>
                    </h4>
                </div>
                <div class="modal-body">
                    <form>
                        <div class="form-group">
                            <label for="email-to">Format&nbsp;:&nbsp;</label>
                            <label class="checkbox-inline">
                                <input type="checkbox" value="pdf" name="output" id="email-pdf" class="formats" checked="checked" > PDF
                            </label>
                            <label class="checkbox-inline">
                                <input type="checkbox" value="csv" name="output" id="email-csv" class="formats"> CSV
                            </label>
                        </div>
                        <div class="form-group">
                            <label for="email-to">To</label>
                            <input type="text" class="form-control" id="email-to" value="${sessionScope.SPRING_SECURITY_CONTEXT.authentication.principal.emailAddress};" required>
                        </div>
                        <div class="form-group">
                            <label for="email-subject">Subject</label>
                            <input type="text" class="form-control" id="email-subject">
                        </div>
                        <div class="form-group">
                            <label for="email-body">Body</label>
                            <textarea class="form-control" id="email-body"></textarea>
                        </div>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" value = "schedule" name = "schedule" id="scheduleReport"> Schedule Reports
                            </label>
                        </div>
                        <div class="form-group hidden">
                            <label for="save-report-name">Report Name</label>
                            <input type="text" class="form-control" id="save-report-name" placeholder="Provide a name to you report schedule">
                        </div>
                    </form>
                </div>
                <div class="modal-footer">
                    <div class="form-group text-center">
                        <div class="col-sm-6 col-sm-offset-3">
                            <button class="btn btn-default" id="emailing-btn">
                                <i class="fa fa-envelope"></i> <span>Email now</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- End of Modal for scheduling -->
    
    <div class="modal" id="scheduling-modal" data-backdrop="static">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only">Close</span>
                    </button>
                    <h4 class="modal-title">
                        <b>
                            <i>Report Scheduling</i>
                        </b>
                    </h4>
                </div>
                <div class="modal-body">
                    <form class="form-horizontal" id="scheduling-form">
                        <div class="form-group">
                            <label class="col-xs-3 control-label">Repeats :</label>
                            <div class="col-xs-8">
                                <select class="form-control" name="Frequency" id="repeatOrder">
                                    <option value="Weekly">Weekly</option>
                                    <option value="Daily">Daily</option>
                                    <option value="Monthly">Monthly</option>
                                    <option value="Yearly">Yearly</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-xs-3 control-label">Repeats every:</label>
                            <div class="col-xs-4">
                                <select class="form-control" name="RepeatsEvery" id="repeatEvery">
                                </select>
                            </div>
                            <label class="col-xs-5 control-label" style="text-align:left;margin-left:-15px"><span id="repeatOrder-text">week</span>(s)</label>
                        </div>
                        <div class="form-group" id="repeatOn">
                            <label class="col-xs-3 control-label" id="repeatText">Repeat On :</label>
                            <div class="col-xs-9" id="weekMonthSelector">
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Sunday" name="DaysofWeek" id="sunday"> S
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Monday" name="DaysofWeek" id="monday"> M
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Tuesday" name="DaysofWeek" id="tuesday"> T
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Wednesday" name="DaysofWeek" id="wednesday"> W 
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Thursday" name="DaysofWeek" id="thursday"> T
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Friday" name="DaysofWeek" id="friday"> F 
                                </label>
                                <label class="checkbox-inline">
                                    <input type="checkbox" class="weeks" value="Saturday" name="DaysofWeek" id="saturday"> S
                                </label>
                            </div>
                        </div>
                        <div class="form-group hidden" id="repeatBy">
                            <label class="col-xs-3 control-label" id="repeatText">Repeat By :</label>
                            <div class="col-xs-9" id="weekMonthSelector">
                                <label class="dayofmonth radio-inline" id="dayofmonth">
                                    <input type="radio" name="RepeatBy" class="monthlyCheck" checked id="dayOfMonth" value="dayOfTheMonth"> day of the month
                                </label>
                                <label class="dayofweek radio-inline" id="dayofweek">
                                    <input type="radio" name="RepeatBy" class="monthlyCheck" id="dayOfWeek" value="dayOfTheWeek"> day of the week
                                </label>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label">Starts On :</label>
                            <div class="col-sm-8" id="start">
                                <input type="text" class="form-control" name="StartDate" id="startsOn">    
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-sm-3 control-label">Ends :</label>
                            <div class="col-sm-9" id="ends">
                                <div class="radio">
                                   <label>
                                      <input type="radio" class="ends" name="endsRadio" id="never" 
                                        value="Never" checked> Never
                                   </label>
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-2 col-sm-offset-3">
                                <div class="radio">
                                    <label>
                                        <input type="radio" name="endsRadio" class="ends" id="endsAfter" value="After"> After  
                                    </label>
                                </div>
                            </div>
                            <div class="col-sm-3">
                                <input type="text" class="form-control" id="endsAfterOccurrences" disabled="disabled" value="35" name="EndAfterExecutions">
                            </div>
                            <label class="col-sm-3 control-label" style="text-align:left;margin-left:-15px">Occurences</label>
                        </div>
                        <div class="form-group">
                            <div class="col-sm-2 col-sm-offset-3">
                                <div class="radio">
                                    <label>
                                        <input type="radio" class="ends" name="endsRadio" id="endsOn" value="On"> On 
                                    </label>
                                </div>
                            </div>
                            <div class="col-sm-5">
                                <input type="text" class="form-control" name="EndDate" id="endsOnDate" disabled="disabled">
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="col-xs-3 control-label">Summary :</label>
                            <label class="col-xs-8 control-label text-capitalize" style="text-align:left"><span id="schedule-summary"></span></label>
                        </div>
                    </form>
                </div>
                <div class="modal-footer text-center">
                    <div class="form-group">
                        <div class="col-sm-6 col-sm-offset-3">
                            <button class="btn btn-default btn-block" id="scheduleButton">Schedule</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- End of Modal for scheduling -->

    <!-- Modal for FileSystem Actions -->
    <div class="modal fade" id="fs-actions-modal">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">
                        <span aria-hidden="true">&times;</span>
                        <span class="sr-only">Close</span>
                    </button>
                    <h4 class="modal-title"></h4>
                </div>
                <div class="modal-body">
                    <div class="fs-action-form new-folder-form">
                        <form class="form-horizontal" id="new-folder-form">
                            <div class="form-group">
                                <label class="col-xs-4 control-label">Enter Folder Name</label>
                                <div class="col-xs-6">
                                    <input type="text" class="form-control" name="folderName" placeholder="Untitled">
                                </div>
                            </div>
                        </form>
                    </div>
                    <div class="fs-action-form move-form">
                        <ul id="move-tree" class="filetree"></ul>
                    </div>
                    <div class="fs-action-form rename-list">
                        <table class="table table-striped">
                            <thead>
                                <th>Old Name</th>
                                <th>New Name</th>
                            </thead>
                            <tbody id="rename"></tbody>
                        </table>
                    </div>
                    <div class="fs-action-form delete-block">
                        <p>Are you sure, you want to delete the selected files?</p>
                    </div>
                    <div class="fs-action-form export-block">
                        <p>You are about to export selected files.</p>
                    </div>
                    <div class="fs-action-form import-block">
                        <div>
                            <span class="btn btn-success fileinput-button">
                                <i class="glyphicon glyphicon-plus"></i>
                                <span>Select files...</span>
                                <!-- The file input field used as target for the file upload widget -->
                                <input id="fileupload" type="file" name="file" accept=".zip">
                            </span>
                        </div>
                        <div>
                            <br>
                            <div class="progress hidden" id="fileupload-progress">
                                <div class="progress-bar" role="progressbar" id="fileupload-progress-bar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
                                0%
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="no-selection">
                        <p>Invalid files/folders selected</p>
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-success fs-ok" id="fs-actions-form--submit">OK</button>
                    <button class="btn btn-danger" data-dismiss="modal">Cancel</button>
                </div>
            </div>
        </div>
    </div>
    <!-- Modal for FileSystem Actions -->

    <!-- Home Template -->
    <script type="text/html" id="home-tpl" data-title="Data Quality Report" data-icon="line-chart">
        <div class="row">
            <div class="col-sm-4">
                <div class="clearfix">
                    <a href="./#!/new-report/" class="home-link">
                        <span class="fa fa-3x fa-bar-chart pull-left"></span>
                        <h4> New Report <br><small>Create new report from template</small></h4>
                    </a>
                </div>
                <div class="clearfix">
                    <a href="./#!/my-library/" class="home-link">
                        <span class="fa fa-3x fa-book pull-left"></span>
                        <h4> My library <br><small>Choose from existing reports</small></h4>
                    </a>
                </div>
                <div class="clearfix">
                    <a href="./#!/favourites/" class="home-link">
                        <span class="fa fa-3x fa-star pull-left"></span>
                        <h4> Favourites <br><small>List favorite reports</small></h4>
                    </a>
                </div>
                <div class="clearfix">
                    <a href="./#!/saved-reports/" class="home-link">
                        <span class="fa fa-3x fa-archive pull-left"></span>
                        <h4> Saved Reports <br><small>List of all save dreports</small></h4>
                    </a>
                </div>
            </div>
        </div>
    </script>
    <!-- End of Home Template -->
    
    <!-- New Report Template -->
    <script type="text/html" id="new-report-tpl" data-title="New Report" data-icon="bar-chart">
        <ul id="new-report-list"></ul>  
    </script>
    <!-- End ofNew Report Template -->
    
    <!-- My library Template -->
    <script type="text/html" id="my-library-tpl" data-title="My library" data-icon="book">
        <ul id="my-library-list"></ul>
    </script>
    <!-- End of My library Template -->
    
    <!-- Favourites Template -->
    <script type="text/html" id="favourites-tpl" data-title="Favourites" data-icon="star">
        <ul id="favourites-list"></ul>
    </script>
    <!-- End of Favourites Template -->
    
    <!-- Saved Reports Template -->
    <script type="text/html" id="saved-reports-tpl" data-title="Saved Reports" data-icon="archive">
        <table class="table table-bordered">
            <thead>
                <th><input type="checkbox" id="selectAllSavedReports"></th>
                <th>Name of Report</th>
                <th>File Name</th>
                <th>File Format</th>
                <th>Save/Run Date</th>
            </thead>
            <tbody id="saved-reports-list"></tbody>
        </table>
    </script>
    <!-- End of Saved Reports Template -->
    <script type="text/javascript" src="js/plugins/fileupload.js"></script>
    <script type="text/javascript" src="js/app.js"></script>
</body>
</html>
