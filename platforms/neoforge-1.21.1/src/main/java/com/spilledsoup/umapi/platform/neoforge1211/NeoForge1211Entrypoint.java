package com.spilledsoup.umapi.platform.neoforge1211;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1211Entrypoint {

    public NeoForge1211Entrypoint() {
        UMAPI.initialise(new NeoForge1211Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.1.");
    }
}
