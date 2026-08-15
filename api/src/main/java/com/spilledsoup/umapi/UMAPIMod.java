package com.spilledsoup.umapi;

import com.spilledsoup.umapi.content.ContentRegistry;

public interface UMAPIMod {
    default void defineContent(ContentRegistry content) {
    }

    void initialise();
}
