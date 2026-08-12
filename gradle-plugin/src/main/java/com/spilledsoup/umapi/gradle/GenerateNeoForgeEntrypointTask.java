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

public abstract class GenerateNeoForgeEntrypointTask extends DefaultTask {
    static final String PACKAGE_NAME = "com.spilledsoup.umapi.generated.neoforge1201";
    static final String CLASS_NAME = "UMAPINeoForgeEntrypoint";

    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getModEntrypoint();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        try {
            var packageDirectory = getOutputDirectory().get()
                    .getAsFile()
                    .toPath()
                    .resolve(PACKAGE_NAME.replace('.', '/'));

            Files.createDirectories(packageDirectory);
            Files.writeString(
                    packageDirectory.resolve(CLASS_NAME + ".java"),
                    render(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not generate NeoForge entrypoint.",
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
                PACKAGE_NAME,
                quoteJavaString(getModId().get()),
                CLASS_NAME,
                CLASS_NAME,
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
