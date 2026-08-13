package com.spilledsoup.umapi.platform.forge1204;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1204Entrypoint {

    public Forge1204Entrypoint() {
        UMAPI.initialise(new Forge1204Platform());
        UMAPI.logger().info("UMAPI Forge 1.20.4 platform initialised.");
    }
}
