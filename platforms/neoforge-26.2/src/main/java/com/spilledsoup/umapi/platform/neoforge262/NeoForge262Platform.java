package com.spilledsoup.umapi.platform.neoforge262;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;
import com.spilledsoup.umapi.platform.shared.neoforge1204plus.NeoForgeEvents;
import com.spilledsoup.umapi.platform.shared.neoforgeplayer.GameProfileNameNeoForgePlayer;

public final class NeoForge262Platform extends AbstractPlatform {
    public NeoForge262Platform() {
        super(ModLoader.NEOFORGE, "26.2", new NeoForgeEvents(GameProfileNameNeoForgePlayer::new), new Slf4jLogger());
    }
}
