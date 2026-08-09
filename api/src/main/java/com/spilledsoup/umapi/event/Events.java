package com.spilledsoup.umapi.event;

import com.spilledsoup.umapi.entity.Player;

import java.util.function.Consumer;

public interface Events {
    void onPlayerJoin(Consumer<Player> callback);
}