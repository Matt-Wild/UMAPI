package com.spilledsoup.umapi.platform.shared.forge1192plus;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.function.Consumer;

public final class ForgeEvents implements Events {

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                callback.accept(new ForgePlayer(player));
            }
        });
    }
}
