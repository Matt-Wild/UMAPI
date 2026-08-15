package com.spilledsoup.umapi.platform.fabric12111;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.fabriclike1192plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric12111Platform extends AbstractPlatform {
    public Fabric12111Platform() {
        super(ModLoader.FABRIC, "1.21.11", new FabricLikeEvents(), new Slf4jLogger());
    }
}
