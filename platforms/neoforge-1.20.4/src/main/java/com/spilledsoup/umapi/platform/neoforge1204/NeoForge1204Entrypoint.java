package com.spilledsoup.umapi.platform.neoforge1204;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1204Entrypoint {

    public NeoForge1204Entrypoint() {
        UMAPI.initialise(new NeoForge1204Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.20.4.");
    }
}
