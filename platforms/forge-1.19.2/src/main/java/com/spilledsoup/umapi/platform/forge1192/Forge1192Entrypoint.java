package com.spilledsoup.umapi.platform.forge1192;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1192Entrypoint {

    public Forge1192Entrypoint() {
        UMAPI.initialise(new Forge1192Platform());
        UMAPI.logger().info("UMAPI Forge 1.19.2 platform initialised.");
    }
}
