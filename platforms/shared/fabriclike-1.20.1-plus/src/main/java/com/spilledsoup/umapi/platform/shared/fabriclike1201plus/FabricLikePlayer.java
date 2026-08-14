package com.spilledsoup.umapi.platform.shared.fabriclike1201plus;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class FabricLikePlayer implements Player {
    private final ServerPlayer player;

    public FabricLikePlayer(ServerPlayer player) {
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
