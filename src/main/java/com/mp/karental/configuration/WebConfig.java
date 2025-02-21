package com.mp.karental.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration class for registering interceptors and other web-related settings.
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registers interceptors for processing incoming requests.
     * <p>
     * In this method, the TraceIdInterceptor is registered to be applied to all incoming requests.
     * This interceptor is responsible for generating or propagating traceId values for logging
     * and debugging purposes.
     * </p>
     *
     * @param registry the InterceptorRegistry used to register interceptors.
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //Register TraceIdInterceptor for all request
//        registry.addInterceptor(new TraceIdInterceptor());
//    }
}
