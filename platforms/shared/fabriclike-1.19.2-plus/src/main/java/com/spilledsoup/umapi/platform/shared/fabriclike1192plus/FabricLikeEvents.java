package com.spilledsoup.umapi.platform.shared.fabriclike1192plus;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.function.Consumer;

public final class FabricLikeEvents implements Events {

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            callback.accept(new FabricLikePlayer(handler.player));
        });
    }
}
