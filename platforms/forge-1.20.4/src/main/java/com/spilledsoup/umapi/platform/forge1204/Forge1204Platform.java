package com.spilledsoup.umapi.platform.forge1204;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.forge120x.Forge120xEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

final class Forge1204Platform extends AbstractPlatform {
    Forge1204Platform() {
        super(new Forge120xEvents(), new Slf4jLogger());
    }
}
