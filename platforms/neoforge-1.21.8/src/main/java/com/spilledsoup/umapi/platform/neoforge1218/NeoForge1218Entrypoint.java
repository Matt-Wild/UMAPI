package com.spilledsoup.umapi.platform.neoforge1218;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1218Entrypoint {

    public NeoForge1218Entrypoint() {
        UMAPI.initialise(new NeoForge1218Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.8.");
    }
}
