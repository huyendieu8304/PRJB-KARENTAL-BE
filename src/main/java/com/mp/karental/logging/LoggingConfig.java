package com.mp.karental.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * This class is responsible for logging configuration in the application
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Configuration
public class LoggingConfig {
    private final int MAX_HISTORY = 7;
    private final FileSize MAX_FILE_SIZE = FileSize.valueOf("10MB");

    public void setupLogging() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        //Connect to console
//        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
//        consoleAppender.setContext(context); //set up Logger Context
//        consoleAppender.start();

        PatternLayoutEncoder fileEncoder = new PatternLayoutEncoder();
        fileEncoder.setContext(context);
        fileEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n");
        fileEncoder.start();

        //Write log to file and roll file depend on time or size
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(context);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.setFile("logs/app.log"); //default logging file

        //setting time for rolling
        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender); //connet to file appender
        rollingPolicy.setFileNamePattern("logs/app.%d{yyyy-MM-dd}.log"); //naming for file when rolling
        rollingPolicy.setMaxHistory(MAX_HISTORY); //keep n day history, file older than 7 day will be deleted
        rollingPolicy.start();

        //setting size for rolling
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(context);
        triggeringPolicy.setMaxFileSize(MAX_FILE_SIZE);
        triggeringPolicy.start();

        //Assign policies to appender
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        fileAppender.start();

        //Get root logger and adding appenders
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
        rootLogger.setLevel(Level.INFO);

    }
}
