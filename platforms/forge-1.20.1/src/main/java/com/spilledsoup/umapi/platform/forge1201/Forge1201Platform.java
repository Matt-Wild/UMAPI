package com.spilledsoup.umapi.platform.forge1201;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.forge1192plus.ForgeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

final class Forge1201Platform extends AbstractPlatform {
    Forge1201Platform() {
        super(ModLoader.FORGE, "1.20.1", new ForgeEvents(), new Slf4jLogger());
    }
}
