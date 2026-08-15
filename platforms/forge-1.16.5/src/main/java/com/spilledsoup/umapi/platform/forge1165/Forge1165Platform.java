package com.spilledsoup.umapi.platform.forge1165;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

final class Forge1165Platform extends AbstractPlatform {
    Forge1165Platform() {
        super(ModLoader.FORGE, "1.16.5", new Forge1165Events(), new Slf4jLogger());
    }
}
