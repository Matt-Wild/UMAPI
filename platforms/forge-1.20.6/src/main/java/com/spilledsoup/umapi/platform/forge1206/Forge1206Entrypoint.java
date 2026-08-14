package com.spilledsoup.umapi.platform.forge1206;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1206Entrypoint {

    public Forge1206Entrypoint() {
        UMAPI.initialise(new Forge1206Platform());
        UMAPI.logger().info("UMAPI Forge 1.20.6 platform initialised.");
    }
}
