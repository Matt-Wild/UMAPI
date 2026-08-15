package com.spilledsoup.umapi.platform;

import com.spilledsoup.umapi.content.ContentRegistry;
import com.spilledsoup.umapi.content.runtime.ReflectiveItemRegistrar;
import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.runtime.RuntimeEnvironment;

import java.util.Objects;

public abstract class AbstractPlatform implements Platform {
    private final RuntimeEnvironment environment;
    private final Events events;
    private final Logger logger;

    protected AbstractPlatform(
            ModLoader loader,
            String minecraftVersion,
            Events events,
            Logger logger
    ) {
        this(RuntimeEnvironment.of(loader, minecraftVersion), events, logger);
    }

    protected AbstractPlatform(RuntimeEnvironment environment, Events events, Logger logger) {
        this.environment = Objects.requireNonNull(environment, "environment");
        this.events = Objects.requireNonNull(events, "events");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public final RuntimeEnvironment environment() {
        return environment;
    }

    @Override
    public final Events events() {
        return events;
    }

    @Override
    public final Logger logger() {
        return logger;
    }

    @Override
    public void registerContent(ContentRegistry content, Object context) {
        ReflectiveItemRegistrar.register(content, context);
    }
}
