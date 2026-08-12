package com.spilledsoup.umapi.platform.neoforge1201;

import com.spilledsoup.umapi.entity.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class NeoForgePlayer implements Player {

    private final ServerPlayer player;

    public NeoForgePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.m_36316_().getName();
    }

    @Override
    public void sendMessage(String message) {
        player.m_213846_(Component.m_237113_(message));
    }
}
