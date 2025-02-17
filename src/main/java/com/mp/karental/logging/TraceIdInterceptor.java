package com.mp.karental.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor used to manage the traceId for each request.
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
public class TraceIdInterceptor implements HandlerInterceptor {

    /**
     * The key used to store the traceId in the Mapped Diagnostic Context (MDC).
     */
    private static final String TRACE_ID = "traceId";
    /**
     * The name of the header used to transfer the traceId between the client and the server.
     */
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * This method is called before the request is handled.
     * <p>
     * It checks the client request header for an existing traceId. If the traceId is not found or is empty,
     * a new traceId is generated. The traceId is then:
     * <ul>
     *     <li>Stored in the MDC for logging.</li>
     *     <li>Added to the response header to be sent back to the client.</li>
     * </ul>
     *
     * @param request  the HttpServletRequest of the current request.
     * @param response the HttpServletResponse of the current response.
     * @param handler  the handler object (could be a controller or any other handler) for the request.
     * @return {@code true} to continue processing the request; {@code false} to abort.
     * @throws Exception if an error occurs during the pre-handling process.
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //check header from client, if it not contain traceId, create new one
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        //put trace id into Mapped Diagnostic Context so that log pattern can get it
        MDC.put(TRACE_ID, traceId);

        //add trace id to response's header
        response.setHeader(TRACE_ID_HEADER, traceId);
        return true;
    }

    /**
     * This method is called after the request has been processed and the response has been produced.
     * <p>
     * After the response is sent to the client (regardless of whether an exception was thrown),
     * the traceId is removed from the MDC to prevent it from affecting subsequent requests.
     * </p>
     *
     * @param request  the HttpServletRequest of the current request.
     * @param response the HttpServletResponse of the current response.
     * @param handler  the handler object (could be a controller or any other handler) for the request.
     * @param ex       the Exception thrown during request processing, or {@code null} if no exception occurred.
     * @throws Exception if an error occurs during the after-completion process.
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //remove trace id from MDC after complete request, whether exception is thrown or not
        MDC.remove(TRACE_ID);
    }
}
