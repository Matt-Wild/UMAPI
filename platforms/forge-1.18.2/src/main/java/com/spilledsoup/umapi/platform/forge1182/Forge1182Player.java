package com.spilledsoup.umapi.platform.forge1182;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

final class Forge1182Player implements Player {
    private final ServerPlayer player;

    Forge1182Player(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(new TextComponent(message), player.getUUID());
    }
}
