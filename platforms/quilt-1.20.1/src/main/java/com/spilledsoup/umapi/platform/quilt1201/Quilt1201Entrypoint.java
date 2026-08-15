package com.spilledsoup.umapi.platform.quilt1201;

import com.spilledsoup.umapi.UMAPI;
import com.spilledsoup.umapi.UMAPIMod;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class Quilt1201Entrypoint implements ModInitializer {

    @Override
    public void onInitialize(ModContainer mod) {
        UMAPI.initialise(new Quilt1201Platform());
        UMAPI.logger().info("UMAPI initialised for Quilt 1.20.1.");

        QuiltLoader.getEntrypointContainers("umapi", UMAPIMod.class)
                .forEach(container -> UMAPI.loadMod(
                        container.getProvider().metadata().id(),
                        container.getEntrypoint()
                ));
    }
}
