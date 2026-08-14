package com.spilledsoup.umapi.platform.fabric1213;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.fabriclike1201plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1213Platform extends AbstractPlatform {
    public Fabric1213Platform() {
        super(new FabricLikeEvents(), new Slf4jLogger());
    }
}
