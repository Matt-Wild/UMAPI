package com.spilledsoup.umapi.platform.quilt1201;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.fabriclike1192plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Quilt1201Platform extends AbstractPlatform {
    public Quilt1201Platform() {
        super(ModLoader.QUILT, "1.20.1", new FabricLikeEvents(), new Slf4jLogger());
    }
}
