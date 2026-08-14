package com.spilledsoup.umapi.platform.neoforge12110;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge12110Entrypoint {

    public NeoForge12110Entrypoint() {
        UMAPI.initialise(new NeoForge12110Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.10.");
    }
}
