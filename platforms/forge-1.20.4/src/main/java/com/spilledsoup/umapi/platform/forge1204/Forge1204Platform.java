package com.spilledsoup.umapi.platform.forge1204;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.forge1192plus.ForgeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

final class Forge1204Platform extends AbstractPlatform {
    Forge1204Platform() {
        super(ModLoader.FORGE, "1.20.4", new ForgeEvents(), new Slf4jLogger());
    }
}
