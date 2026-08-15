package com.spilledsoup.umapi.platform.fabric1213;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.fabriclike1192plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1213Platform extends AbstractPlatform {
    public Fabric1213Platform() {
        super(ModLoader.FABRIC, "1.21.3", new FabricLikeEvents(), new Slf4jLogger());
    }
}
