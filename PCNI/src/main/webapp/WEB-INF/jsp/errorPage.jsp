<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <meta name="decorator" content="minimal" />
        <link rel="shortcut icon" href="favicon.ico">
        <title>Data Quality Reports | Error Page</title>
        <link rel="stylesheet" href="css/styles.css">
    </head>
    <body>
        <div class="container">
            <div class="callout callout-danger">
                <h4>Oops! Something went wrong. Please see your system administrator</h4>
                <c:if test="${!empty requestScope.message}">
                    <p><c:out value="${requestScope.message}"/></p>
                </c:if>
                <c:if test="${!empty requestScope.errorMessage}">
                    <p><c:out value="${requestScope.errorMessage}"/></p>
                </c:if>
            </div>
        </div>
    </body>
</html>
