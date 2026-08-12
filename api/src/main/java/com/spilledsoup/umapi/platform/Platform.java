package com.spilledsoup.umapi.platform;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;

public interface Platform {
    Events events();

    Logger logger();
}
