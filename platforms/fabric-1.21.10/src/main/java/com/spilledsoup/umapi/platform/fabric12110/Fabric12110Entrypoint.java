package com.spilledsoup.umapi.platform.fabric12110;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric12110Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric12110Platform());
        UMAPI.logger().info("UMAPI initialised for Fabric 1.21.10.");

        FabricLoader.getInstance()
                .getEntrypointContainers("umapi", UMAPIMod.class)
                .forEach(container -> UMAPI.loadMod(
                        container.getProvider().getMetadata().getId(),
                        container.getEntrypoint()
                ));
    }
}
