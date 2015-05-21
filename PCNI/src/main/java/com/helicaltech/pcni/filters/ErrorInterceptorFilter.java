package com.helicaltech.pcni.filters;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * The filter class is useful to log the exceptions to log files for debugging
 * purposes.
 * <p/>
 * Any uncaught exception or runtime exception or error will be logged to the
 * log files for debugging purposes preserving the original exception
 * stacktrace.
 * <p/>
 * The original request and response will be preserved and forwarded to the
 * custom error page
 * <p/>
 * Created by author on 31-10-2014.
 *
 * @author Rajasekhar
 * @since 1.0
 */

public class ErrorInterceptorFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(Thread.currentThread() + " : " + ErrorInterceptorFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    /**
     * Logs the stack trace to the log files with original exception that has
     * not been handled and also forwards the request to the errorPage.
     *
     * @param request  The request object
     * @param response The response object
     * @param chain    The filterChain object
     * @throws IOException      The java.io.IOException
     * @throws ServletException The javax.servlet.ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Throwable exception) {
            String rootCauseMessage = ExceptionUtils.getRootCauseMessage(exception);
            request.setAttribute("errorMessage", rootCauseMessage);
            if (exception instanceof RuntimeException) {
                logger.error("A Runtime Exception has occurred. The cause is " +
                        rootCauseMessage, exception);
                throw new RuntimeException();
            }
            if (exception instanceof Exception) {
                logger.error("An Exception has occurred. The cause is " + rootCauseMessage,
                        exception);
            } else {
                logger.error("An Error has occurred. The cause is " + rootCauseMessage, exception);
            }
            throw new RuntimeException();
        }
    }

    @Override
    public void destroy() {
    }
}