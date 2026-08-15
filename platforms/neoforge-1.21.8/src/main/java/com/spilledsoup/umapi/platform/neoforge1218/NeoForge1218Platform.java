package com.spilledsoup.umapi.platform.neoforge1218;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;
import com.spilledsoup.umapi.platform.shared.neoforge1204plus.NeoForgeEvents;
import com.spilledsoup.umapi.platform.shared.neoforgeplayer.GameProfileGetNameNeoForgePlayer;

public final class NeoForge1218Platform extends AbstractPlatform {
    public NeoForge1218Platform() {
        super(ModLoader.NEOFORGE, "1.21.8", new NeoForgeEvents(GameProfileGetNameNeoForgePlayer::new), new Slf4jLogger());
    }
}
