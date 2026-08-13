package com.spilledsoup.umapi.platform.neoforge1204;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class NeoForge1204Platform extends AbstractPlatform {
    public NeoForge1204Platform() {
        super(new NeoForgeEvents(), new Slf4jLogger());
    }
}
