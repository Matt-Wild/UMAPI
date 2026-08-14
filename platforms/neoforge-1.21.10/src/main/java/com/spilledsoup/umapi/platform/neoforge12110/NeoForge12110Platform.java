package com.spilledsoup.umapi.platform.neoforge12110;

import com.spilledsoup.umapi.platform.AbstractPlatform;
import com.spilledsoup.umapi.platform.shared.logging.Slf4jLogger;
import com.spilledsoup.umapi.platform.shared.neoforge1204plus.NeoForgeEvents;
import com.spilledsoup.umapi.platform.shared.neoforgeplayer.GameProfileNameNeoForgePlayer;

public final class NeoForge12110Platform extends AbstractPlatform {
    public NeoForge12110Platform() {
        super(new NeoForgeEvents(GameProfileNameNeoForgePlayer::new), new Slf4jLogger());
    }
}
