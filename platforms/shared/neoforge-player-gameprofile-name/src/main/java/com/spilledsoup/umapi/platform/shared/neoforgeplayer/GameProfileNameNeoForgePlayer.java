package com.spilledsoup.umapi.platform.shared.neoforgeplayer;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GameProfileNameNeoForgePlayer implements Player {
    private final ServerPlayer player;

    public GameProfileNameNeoForgePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().name();
    }

    @Override
    public void sendMessage(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
