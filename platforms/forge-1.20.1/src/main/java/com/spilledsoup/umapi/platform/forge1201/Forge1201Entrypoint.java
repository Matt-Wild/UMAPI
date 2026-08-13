package com.spilledsoup.umapi.platform.forge1201;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1201Entrypoint {

    public Forge1201Entrypoint() {
        UMAPI.initialise(new Forge1201Platform());
        UMAPI.logger().info("UMAPI Forge 1.20.1 platform initialised.");
    }
}
