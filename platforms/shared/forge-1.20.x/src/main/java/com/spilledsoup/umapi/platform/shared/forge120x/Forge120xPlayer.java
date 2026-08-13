package com.spilledsoup.umapi.platform.shared.forge120x;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class Forge120xPlayer implements Player {
    private final ServerPlayer player;

    public Forge120xPlayer(ServerPlayer player) {
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
