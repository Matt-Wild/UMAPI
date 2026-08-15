package com.spilledsoup.umapi.platform.fabric1192;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric1192Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric1192Platform());
        UMAPI.logger().info("UMAPI initialised for Fabric 1.19.2.");

        FabricLoader.getInstance()
                .getEntrypointContainers("umapi", UMAPIMod.class)
                .forEach(container -> UMAPI.loadMod(
                        container.getProvider().getMetadata().getId(),
                        container.getEntrypoint()
                ));
    }
}
