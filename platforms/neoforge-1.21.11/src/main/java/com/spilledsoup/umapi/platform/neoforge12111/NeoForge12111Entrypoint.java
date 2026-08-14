package com.spilledsoup.umapi.platform.neoforge12111;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge12111Entrypoint {

    public NeoForge12111Entrypoint() {
        UMAPI.initialise(new NeoForge12111Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.11.");
    }
}
