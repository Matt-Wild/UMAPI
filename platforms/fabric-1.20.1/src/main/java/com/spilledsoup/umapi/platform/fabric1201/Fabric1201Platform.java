package com.spilledsoup.umapi.platform.fabric1201;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.fabriclike120x.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1201Platform extends AbstractPlatform {
    public Fabric1201Platform() {
        super(new FabricLikeEvents(), new Slf4jLogger());
    }
}
