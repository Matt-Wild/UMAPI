package com.spilledsoup.umapi.platform.fabric2612;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.fabriclike1192plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric2612Platform extends AbstractPlatform {
    public Fabric2612Platform() {
        super(ModLoader.FABRIC, "26.1.2", new FabricLikeEvents(), new Slf4jLogger());
    }
}
