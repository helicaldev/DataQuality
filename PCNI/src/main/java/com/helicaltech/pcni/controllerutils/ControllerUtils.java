package com.helicaltech.pcni.controllerutils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helicaltech.pcni.exceptions.RequiredParameterIsNullException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

/**
 * Created by author on 01-Jan-15.
 *
 * @author Rajasekhar
 */
public class ControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : " + ControllerUtils.class);

    public static boolean isAjax(@NotNull HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    public static void checkForNullsAndEmptyParameters(@NotNull Map<String, String> parameters) {
        Set<Map.Entry<String, String>> entries = parameters.entrySet();

        for (Map.Entry<String, String> entry : entries) {
            String value = entry.getValue();
            if ((value == null) || ("".equals(value)) || (value.trim().length() < 0)) {
                throw new RequiredParameterIsNullException(String.format("The parameter %s is " +
                        "null or empty. Invalid request.", entry.getKey()));
            }
        }
    }

    public static void handleFailure(@NotNull HttpServletResponse response,
                                     boolean isAjax, Exception exception) throws
            IOException {
        if (isAjax) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(exception);
            logger.error("There was a problem in serving the request. The cause is " +
                    rootCauseMessage, exception);
            handleAjaxRuntimeException(response, rootCauseMessage);
        } else {
            //Now ErrorInterceptorFilter and Tomcat deal with the request
            throw new RuntimeException("Unable to process the request. Something went terribly wrong.", exception);
        }
    }

    public static void handleSuccess(@NotNull HttpServletResponse response,
                                     boolean isAjax, String executionResult) throws
            IOException {
        if (isAjax) {
            handleAjaxSuccess(response, executionResult);
        } else {
            handleNormalRequestSuccess(response, executionResult);
        }
    }

    private static void handleNormalRequestSuccess(@NotNull HttpServletResponse response,
                                                   String json) throws IOException {
        response.setContentType("text/html");
        write(response, json);
    }

    private static void handleAjaxSuccess(@NotNull HttpServletResponse response,
                                          String json) throws IOException {
        response.setContentType("application/json");
        write(response, json);
    }

    private static void write(@NotNull HttpServletResponse response,
                              String json) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private static void handleAjaxRuntimeException(@NotNull HttpServletResponse response,
                                                   @NotNull String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(message);
        response.flushBuffer();
    }
}