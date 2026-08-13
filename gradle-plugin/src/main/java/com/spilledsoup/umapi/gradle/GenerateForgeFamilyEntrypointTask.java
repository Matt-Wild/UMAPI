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
        return """
                package %s;

                import com.spilledsoup.umapi.UMAPIMod;
                import net.minecraftforge.fml.common.Mod;
                import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
                import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

                @Mod(%s)
                public final class %s {

                    public %s() {
                        FMLJavaModLoadingContext.get()
                                .getModEventBus()
                                .addListener(this::initialise);
                    }

                    private void initialise(FMLCommonSetupEvent event) {
                        UMAPIMod mod = new %s();
                        mod.initialise();
                    }
                }
                """.formatted(
                getEntrypointPackage().get(),
                quoteJavaString(getModId().get()),
                getEntrypointClassName().get(),
                getEntrypointClassName().get(),
                getModEntrypoint().get()
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
