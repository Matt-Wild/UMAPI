package com.spilledsoup.umapi.platform.shared.neoforge1204plus;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.function.Consumer;
import java.util.function.Function;

public final class NeoForgeEvents implements Events {
    private final Function<ServerPlayer, Player> playerFactory;

    public NeoForgeEvents(Function<ServerPlayer, Player> playerFactory) {
        this.playerFactory = playerFactory;
    }

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                callback.accept(playerFactory.apply(player));
            }
        });
    }
}
