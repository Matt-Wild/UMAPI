package com.spilledsoup.umapi;

import com.spilledsoup.umapi.content.ContentRegistry;
import com.spilledsoup.umapi.event.Events;
import com.spilledsoup.umapi.logging.Logger;
import com.spilledsoup.umapi.platform.Platform;
import com.spilledsoup.umapi.runtime.MinecraftVersion;
import com.spilledsoup.umapi.runtime.ModLoader;
import com.spilledsoup.umapi.runtime.RuntimeEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;

public final class UMAPI {

    private static Platform platform;
    private static final Map<String, ContentRegistry> contentByNamespace = new LinkedHashMap<>();

    private UMAPI() {
    }

    public static void initialise(Platform platform) {
        if (UMAPI.platform != null) {
            throw new IllegalStateException("UMAPI has already been initialised.");
        }

        UMAPI.platform = platform;
    }

    public static void loadMod(UMAPIMod mod) {
        loadMod(mod.getClass().getPackageName(), mod);
    }

    public static void loadMod(String namespace, UMAPIMod mod) {
        declareModContent(namespace, mod);
        registerContent(namespace);
        initialiseMod(mod);
    }

    public static ContentRegistry declareModContent(String namespace, UMAPIMod mod) {
        if (contentByNamespace.containsKey(namespace)) {
            return contentByNamespace.get(namespace);
        }

        ContentRegistry content = ContentRegistry.forMod(namespace);
        mod.defineContent(content);
        contentByNamespace.put(namespace, content);
        return content;
    }

    public static void registerContent(String namespace) {
        registerContent(namespace, null);
    }

    public static void registerContent(String namespace, Object context) {
        ContentRegistry content = contentByNamespace.get(namespace);
        if (content == null) {
            throw new IllegalStateException("No UMAPI content has been declared for " + namespace + ".");
        }

        platform().registerContent(content, context);
    }

    public static void initialiseMod(UMAPIMod mod) {
        mod.initialise();
    }

    public static ContentRegistry content() {
        if (contentByNamespace.size() != 1) {
            throw new IllegalStateException(
                    "UMAPI content() requires exactly one loaded content namespace."
            );
        }

        return contentByNamespace.values().iterator().next();
    }

    public static ContentRegistry content(String namespace) {
        ContentRegistry content = contentByNamespace.get(namespace);
        if (content == null) {
            throw new IllegalStateException("No UMAPI content has been declared for " + namespace + ".");
        }

        return content;
    }

    public static Events events() {
        return platform().events();
    }

    public static Logger logger() {
        return platform().logger();
    }

    public static RuntimeEnvironment environment() {
        return platform().environment();
    }

    public static ModLoader loader() {
        return environment().loader();
    }

    public static MinecraftVersion minecraftVersion() {
        return environment().minecraftVersion();
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
