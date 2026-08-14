package com.spilledsoup.umapi.platform.neoforge1215;

import com.spilledsoup.umapi.UMAPI;
import net.neoforged.fml.common.Mod;

@Mod("umapi")
public final class NeoForge1215Entrypoint {

    public NeoForge1215Entrypoint() {
        UMAPI.initialise(new NeoForge1215Platform());
        UMAPI.logger().info("UMAPI initialised for NeoForge 1.21.5.");
    }
}
