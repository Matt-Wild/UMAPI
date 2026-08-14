package com.spilledsoup.umapi.platform.fabric12111;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.fabriclike1201plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric12111Platform extends AbstractPlatform {
    public Fabric12111Platform() {
        super(new FabricLikeEvents(), new Slf4jLogger());
    }
}
