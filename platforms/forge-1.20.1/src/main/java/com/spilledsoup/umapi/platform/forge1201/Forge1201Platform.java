package com.spilledsoup.umapi.platform.forge1201;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.platform.Platform;

final class Forge1201Platform implements Platform {
    private final Events events = new ForgeEvents();
    private final Logger logger = new ForgeLogger();

    @Override
    public Events events() {
        return events;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
