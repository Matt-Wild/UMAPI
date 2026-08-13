package com.spilledsoup.umapi.platform.quilt1201;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class QuiltPlayer implements Player {

    private final ServerPlayer player;

    public QuiltPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName().getString();
    }

    @Override
    public void sendMessage(String message) {
        player.sendSystemMessage(Component.literal(message));
    }
}
