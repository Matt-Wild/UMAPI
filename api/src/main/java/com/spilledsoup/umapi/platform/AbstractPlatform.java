package com.spilledsoup.umapi.platform;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;

import java.util.Objects;

public abstract class AbstractPlatform implements Platform {
    private final Events events;
    private final Logger logger;

    protected AbstractPlatform(Events events, Logger logger) {
        this.events = Objects.requireNonNull(events, "events");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public final Events events() {
        return events;
    }

    @Override
    public final Logger logger() {
        return logger;
    }
}
