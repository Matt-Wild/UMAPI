package com.spilledsoup.umapi.platform.neoforge262;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge262Entrypoint {

    public NeoForge262Entrypoint() {
        UMAPI.initialise(new NeoForge262Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 26.2.");
    }
}
