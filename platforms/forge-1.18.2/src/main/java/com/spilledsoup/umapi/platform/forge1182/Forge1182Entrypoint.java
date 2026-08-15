package com.spilledsoup.umapi.platform.forge1182;

import com.spilledsoup.umapi.UMAPI;
import net.minecraftforge.fml.common.Mod;

@Mod("umapi")
public final class Forge1182Entrypoint {

    public Forge1182Entrypoint() {
        UMAPI.initialise(new Forge1182Platform());
        UMAPI.logger().info("UMAPI Forge 1.18.2 platform initialised.");
    }
}
