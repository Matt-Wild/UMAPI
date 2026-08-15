package com.spilledsoup.umapi.platform.forge1182;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

final class Forge1182Platform extends AbstractPlatform {
    Forge1182Platform() {
        super(ModLoader.FORGE, "1.18.2", new Forge1182Events(), new Slf4jLogger());
    }
}
