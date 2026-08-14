package com.spilledsoup.umapi.platform.fabric1211;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.fabriclike1201plus.FabricLikeEvents;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class Fabric1211Platform extends AbstractPlatform {
    public Fabric1211Platform() {
        super(new FabricLikeEvents(), new Slf4jLogger());
    }
}
