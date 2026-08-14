package com.spilledsoup.umapi.platform.fabric1218;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric1218Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric1218Platform());
        UMAPI.logger().info("UMAPI initialised for Fabric 1.21.8.");

        FabricLoader.getInstance()
                .invokeEntrypoints(
                        "umapi",
                        UMAPIMod.class,
                        UMAPIMod::initialise
                );
    }
}
