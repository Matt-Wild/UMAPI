package com.spilledsoup.umapi.platform.fabric1201;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.fabriclike1192plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1201Platform extends AbstractPlatform {
    public Fabric1201Platform() {
        super(ModLoader.FABRIC, "1.20.1", new FabricLikeEvents(), new Slf4jLogger());
    }
}
