package com.spilledsoup.umapi.platform;

import com.spilledsoup.umapi.content.ContentRegistry;
import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.runtime.RuntimeEnvironment;

public interface Platform {
    RuntimeEnvironment environment();

    Events events();

    Logger logger();

    default void registerContent(ContentRegistry content, Object context) {
    }
}
