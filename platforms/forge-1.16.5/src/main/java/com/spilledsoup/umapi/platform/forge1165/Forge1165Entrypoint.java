package com.spilledsoup.umapi.platform.forge1165;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1165Entrypoint {

    public Forge1165Entrypoint() {
        UMAPI.initialise(new Forge1165Platform());
        UMAPI.logger().info("UMAPI Forge 1.16.5 platform initialised.");
    }
}
