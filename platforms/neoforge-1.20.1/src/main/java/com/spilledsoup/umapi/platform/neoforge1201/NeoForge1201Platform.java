package com.spilledsoup.umapi.platform.neoforge1201;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.platform.Platform;

public final class NeoForge1201Platform implements Platform {

    private final Events events = new NeoForgeEvents();
    private final Logger logger = new NeoForgeLogger();

    @Override
    public Events events() {
        return events;
    }

    @Override
    public Logger logger() {
        return logger;
    }
}
