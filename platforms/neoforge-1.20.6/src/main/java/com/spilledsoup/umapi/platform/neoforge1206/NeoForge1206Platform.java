package com.spilledsoup.umapi.platform.neoforge1206;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;
import com.spilledsoup.umapi.platform.shared.neoforge1204plus.NeoForgeEvents;
import com.spilledsoup.umapi.platform.shared.neoforgeplayer.GameProfileGetNameNeoForgePlayer;

public final class NeoForge1206Platform extends AbstractPlatform {
    public NeoForge1206Platform() {
        super(ModLoader.NEOFORGE, "1.20.6", new NeoForgeEvents(GameProfileGetNameNeoForgePlayer::new), new Slf4jLogger());
    }
}
