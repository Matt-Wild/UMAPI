plugins {
    `java-gradle-plugin`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

gradlePlugin {
    plugins {
        create("umapiSettings") {
            id = "com.spilledsoup.umapi.settings"
            implementationClass = "com.spilledsoup.umapi.gradle.UMAPISettingsPlugin"
        }
    }
}
