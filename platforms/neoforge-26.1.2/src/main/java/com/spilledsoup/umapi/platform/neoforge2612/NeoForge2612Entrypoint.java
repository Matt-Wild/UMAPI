package com.spilledsoup.umapi.platform.neoforge2612;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge2612Entrypoint {

    public NeoForge2612Entrypoint() {
        UMAPI.initialise(new NeoForge2612Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 26.1.2.");
    }
}
