package com.spilledsoup.umapi.platform.neoforge1213;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1213Entrypoint {

    public NeoForge1213Entrypoint() {
        UMAPI.initialise(new NeoForge1213Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.3.");
    }
}
