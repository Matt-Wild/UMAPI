package com.spilledsoup.umapi.platform.forge1165;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

final class Forge1165Player implements Player {
    private final ServerPlayerEntity player;

    Forge1165Player(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(new StringTextComponent(message), player.getUUID());
    }
}
