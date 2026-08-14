package com.spilledsoup.umapi.platform.neoforge1206;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1206Entrypoint {

    public NeoForge1206Entrypoint() {
        UMAPI.initialise(new NeoForge1206Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.20.6.");
    }
}
