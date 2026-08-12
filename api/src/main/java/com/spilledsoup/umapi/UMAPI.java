package com.spilledsoup.umapi;

import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.platform.Platform;

public final class UMAPI {

    private static Platform platform;

    private UMAPI() {
    }

    public static void initialise(Platform platform) {
        if (UMAPI.platform != null) {
            throw new IllegalStateException("UMAPI has already been initialised.");
        }

        UMAPI.platform = platform;
    }

    public static Events events() {
        return platform().events();
    }

    public static Logger logger() {
        return platform().logger();
    }

    public static String getVersion() {
        String version = UMAPI.class
                .getPackage()
                .getImplementationVersion();

        return version != null ? version : "development";
    }

    private static Platform platform() {
        if (platform == null) {
            throw new IllegalStateException("UMAPI has not been initialised.");
        }

        return platform;
    }
}
