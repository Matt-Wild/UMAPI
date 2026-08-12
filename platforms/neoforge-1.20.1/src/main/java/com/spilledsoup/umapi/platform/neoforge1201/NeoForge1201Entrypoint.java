package com.spilledsoup.umapi.platform.neoforge1201;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1201Entrypoint {

    public NeoForge1201Entrypoint() {
        UMAPI.initialise(new NeoForge1201Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.20.1.");
    }
}
