package com.spilledsoup.umapi;

public final class UMAPI {

    private UMAPI() {
    }

    public static String getVersion() {
        String version = UMAPI.class
                .getPackage()
                .getImplementationVersion();

        return version != null ? version : "development";
    }
}