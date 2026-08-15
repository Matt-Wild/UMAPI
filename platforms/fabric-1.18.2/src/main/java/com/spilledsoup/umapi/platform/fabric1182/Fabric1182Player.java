package com.spilledsoup.umapi.platform.fabric1182;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;

final class Fabric1182Player implements Player {
    private final ServerPlayer player;

    Fabric1182Player(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(new TextComponent(message), player.getUUID());
    }
}
