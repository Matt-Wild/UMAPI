package com.spilledsoup.umapi.platform.forge1206;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.forge120x.Forge120xEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Forge1206Platform extends AbstractPlatform {
    public Forge1206Platform() {
        super(new Forge120xEvents(), new Slf4jLogger());
    }
}
