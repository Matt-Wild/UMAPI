package com.spilledsoup.umapi.platform.neoforge1201;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;

public final class NeoForge1201Platform extends AbstractPlatform {
    public NeoForge1201Platform() {
        super(ModLoader.NEOFORGE, "1.20.1", new NeoForgeEvents(), new Slf4jLogger());
    }
}
