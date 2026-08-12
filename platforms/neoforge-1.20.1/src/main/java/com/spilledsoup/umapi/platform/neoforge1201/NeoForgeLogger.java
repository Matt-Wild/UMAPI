package com.spilledsoup.umapi.platform.neoforge1201;

import com.spilledsoup.umapi.logging.Logger;
import org.slf4j.LoggerFactory;

public final class NeoForgeLogger implements Logger {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger("UMAPI");

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
