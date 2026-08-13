package com.spilledsoup.umapi.platform.fabric1204;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric1204Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric1204Platform());
        UMAPI.logger().info("UMAPI initialised for Fabric 1.20.4.");

        FabricLoader.getInstance()
                .invokeEntrypoints(
                        "umapi",
                        UMAPIMod.class,
                        UMAPIMod::initialise
                );
    }
}
