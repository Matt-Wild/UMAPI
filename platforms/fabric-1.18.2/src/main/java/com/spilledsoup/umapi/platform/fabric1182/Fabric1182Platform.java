package com.spilledsoup.umapi.platform.fabric1182;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1182Platform extends AbstractPlatform {
    public Fabric1182Platform() {
        super(ModLoader.FABRIC, "1.18.2", new Fabric1182Events(), new Slf4jLogger());
    }
}
