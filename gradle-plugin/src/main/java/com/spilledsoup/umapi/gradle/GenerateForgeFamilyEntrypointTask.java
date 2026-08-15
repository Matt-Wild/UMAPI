package com.spilledsoup.umapi.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public abstract class GenerateForgeFamilyEntrypointTask extends DefaultTask {
    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getModEntrypoint();

    @Input
    public abstract Property<String> getEntrypointPackage();

    @Input
    public abstract Property<String> getEntrypointClassName();

    @Input
    public abstract Property<String> getFmlPackageRoot();

    @Input
    public abstract Property<String> getModEventBusParameterType();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        try {
            var packageDirectory = getOutputDirectory().get()
                    .getAsFile()
                    .toPath()
                    .resolve(getEntrypointPackage().get().replace('.', '/'));

            Files.createDirectories(packageDirectory);
            Files.writeString(
                    packageDirectory.resolve(getEntrypointClassName().get() + ".java"),
                    render(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate Forge-family entrypoint.",
                    exception
            );
        }
    }

    private String render() {
        if (!getModEventBusParameterType().get().isBlank()) {
            return renderWithEventBusParameter();
        }

        return renderWithLoadingContextGetter();
    }

    private String renderWithLoadingContextGetter() {
        return """
                package %s;

                import com.spilledsoup.umapi.UMAPI;
                import com.spilledsoup.umapi.UMAPIMod;
                import %s.fml.common.Mod;
                import %s.fml.event.lifecycle.FMLCommonSetupEvent;
                import %s.fml.javafmlmod.FMLJavaModLoadingContext;

                @Mod(%s)
                public final class %s {
                    private final UMAPIMod mod = new %s();

                    public %s() {
                        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

                        UMAPI.declareModContent(%s, mod);
                        UMAPI.registerContent(%s, modEventBus);

                        modEventBus.addListener(this::initialise);
                    }

                    private void initialise(FMLCommonSetupEvent event) {
                        UMAPI.initialiseMod(mod);
                    }
                }
                """.formatted(
                getEntrypointPackage().get(),
                getFmlPackageRoot().get(),
                getFmlPackageRoot().get(),
                getFmlPackageRoot().get(),
                quoteJavaString(getModId().get()),
                getEntrypointClassName().get(),
                getModEntrypoint().get(),
                getEntrypointClassName().get(),
                quoteJavaString(getModId().get()),
                quoteJavaString(getModId().get())
        );
    }

    private String renderWithEventBusParameter() {
        return """
                package %s;

                import com.spilledsoup.umapi.UMAPI;
                import com.spilledsoup.umapi.UMAPIMod;
                import %s.fml.common.Mod;
                import %s.fml.event.lifecycle.FMLCommonSetupEvent;

                @Mod(%s)
                public final class %s {
                    private final UMAPIMod mod = new %s();

                    public %s(%s modEventBus) {
                        UMAPI.declareModContent(%s, mod);
                        UMAPI.registerContent(%s, modEventBus);

                        modEventBus.addListener(this::initialise);
                    }

                    private void initialise(FMLCommonSetupEvent event) {
                        UMAPI.initialiseMod(mod);
                    }
                }
                """.formatted(
                getEntrypointPackage().get(),
                getFmlPackageRoot().get(),
                getFmlPackageRoot().get(),
                quoteJavaString(getModId().get()),
                getEntrypointClassName().get(),
                getModEntrypoint().get(),
                getEntrypointClassName().get(),
                getModEventBusParameterType().get(),
                quoteJavaString(getModId().get()),
                quoteJavaString(getModId().get())
        );
    }

    private static String quoteJavaString(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "\"" + escaped + "\"";
    }
}
