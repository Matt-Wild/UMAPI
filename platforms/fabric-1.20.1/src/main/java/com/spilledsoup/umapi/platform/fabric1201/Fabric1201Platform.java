package com.spilledsoup.umapi.platform.fabric1201;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.platform.Platform;

public final class Fabric1201Platform implements Platform {

    private final Events events = new FabricEvents();

    @Override
    public Events events() {
        return events;
    }
}