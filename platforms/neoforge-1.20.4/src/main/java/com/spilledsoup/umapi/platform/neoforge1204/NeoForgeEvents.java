package com.spilledsoup.umapi.platform.neoforge1204;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.Consumer;

public final class NeoForgeEvents implements Events {

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                callback.accept(new NeoForgePlayer(player));
            }
        });
    }
}
