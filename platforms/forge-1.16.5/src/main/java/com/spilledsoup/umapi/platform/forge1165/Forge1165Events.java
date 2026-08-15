package com.spilledsoup.umapi.platform.forge1165;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.function.Consumer;

final class Forge1165Events implements Events {

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getPlayer() instanceof ServerPlayerEntity) {
                callback.accept(new Forge1165Player((ServerPlayerEntity) event.getPlayer()));
            }
        });
    }
}
