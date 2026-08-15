package com.spilledsoup.umapi.platform.fabric1182;

import com.spilledsoup.umapi.entity.Player;
import com.spilledsoup.umapi.event.Events;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.function.Consumer;

final class Fabric1182Events implements Events {

    @Override
    public void onPlayerJoin(Consumer<Player> callback) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            callback.accept(new Fabric1182Player(handler.player));
        });
    }
}
