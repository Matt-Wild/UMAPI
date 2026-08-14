package com.spilledsoup.umapi.platform.shared.neoforgeplayer;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class GameProfileGetNameNeoForgePlayer implements Player {
    private final ServerPlayer player;

    public GameProfileGetNameNeoForgePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
