package com.spilledsoup.umapi.platform.shared.logging;

import com.spilledsoup.umapi.logging.Logger;
import org.slf4j.LoggerFactory;

public final class Slf4jLogger implements Logger {
    private final org.slf4j.Logger logger;

    public Slf4jLogger() {
        this("UMAPI");
    }

    public Slf4jLogger(String name) {
        logger = LoggerFactory.getLogger(name);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
