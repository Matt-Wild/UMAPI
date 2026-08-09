package com.spilledsoup.umapi.platform.fabric1201;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public final class Fabric1201Entrypoint implements ModInitializer {

    @Override
    public void onInitialize() {
        UMAPI.initialise(new Fabric1201Platform());

        FabricLoader.getInstance()
                .invokeEntrypoints(
                        "umapi",
                        UMAPIMod.class,
                        UMAPIMod::initialise
                );
    }
}