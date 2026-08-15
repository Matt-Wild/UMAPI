package com.spilledsoup.umapi.platform.fabric2612;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric2612Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric2612Platform());
        UMAPI.logger().info("UMAPI initialised for Fabric 26.1.2.");

        FabricLoader.getInstance()
                .getEntrypointContainers("umapi", UMAPIMod.class)
                .forEach(container -> UMAPI.loadMod(
                        container.getProvider().getMetadata().getId(),
                        container.getEntrypoint()
                ));
    }
}
