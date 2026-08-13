package com.spilledsoup.umapi.platform.quilt1201;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.platform.Platform;

public final class Quilt1201Platform implements Platform {

    private final Events events = new QuiltEvents();
    private final Logger logger = new QuiltLogger();

    @Override
    public Events events() {
        return events;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
