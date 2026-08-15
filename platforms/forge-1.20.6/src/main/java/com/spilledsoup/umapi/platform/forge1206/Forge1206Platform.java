package com.spilledsoup.umapi.platform.forge1206;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.forge1192plus.ForgeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Forge1206Platform extends AbstractPlatform {
    public Forge1206Platform() {
        super(ModLoader.FORGE, "1.20.6", new ForgeEvents(), new Slf4jLogger());
    }
}
