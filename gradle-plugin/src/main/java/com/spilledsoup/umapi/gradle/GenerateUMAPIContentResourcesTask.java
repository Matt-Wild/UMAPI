package com.spilledsoup.umapi.gradle;

import com.spilledsoup.umapi.UMAPIMod;
import com.spilledsoup.umapi.content.ContentRegistry;
import com.spilledsoup.umapi.content.ItemContent;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class GenerateUMAPIContentResourcesTask extends DefaultTask {
    @Input
    public abstract Property<String> getModId();

    @Input
    public abstract Property<String> getModEntrypoint();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Classpath
    public abstract ConfigurableFileCollection getModClasspath();

    @Optional
    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getSourceResourcesDirectory();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void generate() {
        ContentRegistry content = loadContent();

        try {
            writeContentResources(content);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not generate UMAPI content resources.", exception);
        }
    }

    private ContentRegistry loadContent() {
        try (URLClassLoader classLoader = new URLClassLoader(classpathUrls(), getClass().getClassLoader())) {
            Class<?> modClass = Class.forName(getModEntrypoint().get(), true, classLoader);
            Object mod = modClass.getConstructor().newInstance();

            if (!(mod instanceof UMAPIMod umapiMod)) {
                throw new IllegalStateException(
                        getModEntrypoint().get() + " does not implement UMAPIMod."
                );
            }

            ContentRegistry content = ContentRegistry.forMod(getModId().get());
            umapiMod.defineContent(content);
            return content;
        } catch (ReflectiveOperationException | IOException exception) {
            throw new IllegalStateException("Could not load UMAPI content declarations.", exception);
        }
    }

    private URL[] classpathUrls() {
        return getModClasspath()
                .getFiles()
                .stream()
                .map(GenerateUMAPIContentResourcesTask::toUrl)
                .toArray(URL[]::new);
    }

    private void writeContentResources(ContentRegistry content) throws IOException {
        Path outputDirectory = getOutputDirectory().get().getAsFile().toPath();
        Path namespaceAssets = outputDirectory.resolve("assets").resolve(content.namespace());

        cleanGeneratedContent(namespaceAssets);

        Map<String, String> languageEntries = new LinkedHashMap<>();

        for (ItemContent item : content.items()) {
            writeItemTexture(namespaceAssets, item);
            writeItemModel(namespaceAssets, item);
            languageEntries.put("item." + item.namespace() + "." + item.id(), item.name());
        }

        writeLanguage(namespaceAssets, languageEntries);
    }

    private void writeItemTexture(Path namespaceAssets, ItemContent item) throws IOException {
        String texturePath = item.texture()
                .select(getMinecraftVersion().get())
                .orElseThrow(() -> new IllegalStateException(
                        "Item " + item.qualifiedId() + " does not declare a texture."
                ))
                .value();

        if (!texturePath.endsWith(".png")) {
            throw new IllegalStateException(
                    "Item texture must be a .png path: " + texturePath
            );
        }

        Path sourceTexture = sourceResource(texturePath);
        Path targetTexture = namespaceAssets
                .resolve("textures")
                .resolve("item")
                .resolve(item.id() + ".png");

        Files.createDirectories(targetTexture.getParent());
        Files.copy(sourceTexture, targetTexture);
    }

    private void writeItemModel(Path namespaceAssets, ItemContent item) throws IOException {
        Path model = namespaceAssets
                .resolve("models")
                .resolve("item")
                .resolve(item.id() + ".json");

        String textureReference = item.namespace() + ":item/" + item.id();
        String json = """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "%s"
                  }
                }
                """.formatted(escapeJson(textureReference));

        Files.createDirectories(model.getParent());
        Files.writeString(model, json, StandardCharsets.UTF_8);
    }

    private void writeLanguage(Path namespaceAssets, Map<String, String> languageEntries) throws IOException {
        if (languageEntries.isEmpty()) {
            return;
        }

        Path languageFile = namespaceAssets
                .resolve("lang")
                .resolve("en_us.json");

        String entries = languageEntries.entrySet()
                .stream()
                .map(entry -> "  \"" + escapeJson(entry.getKey()) + "\": \""
                        + escapeJson(entry.getValue()) + "\"")
                .collect(Collectors.joining(",\n"));

        Files.createDirectories(languageFile.getParent());
        Files.writeString(
                languageFile,
                "{\n" + entries + "\n}\n",
                StandardCharsets.UTF_8
        );
    }

    private Path sourceResource(String path) {
        Path root = getSourceResourcesDirectory().get().getAsFile().toPath().toAbsolutePath().normalize();
        Path source = root.resolve(path).normalize();

        if (!source.startsWith(root)) {
            throw new IllegalStateException("Texture path escapes the resources directory: " + path);
        }

        if (!Files.isRegularFile(source)) {
            throw new IllegalStateException("Texture file does not exist: " + source);
        }

        return source;
    }

    private static void cleanGeneratedContent(Path namespaceAssets) throws IOException {
        deleteIfExists(namespaceAssets.resolve("textures").resolve("item"));
        deleteIfExists(namespaceAssets.resolve("models").resolve("item"));
        Files.deleteIfExists(namespaceAssets.resolve("lang").resolve("en_us.json"));
    }

    private static void deleteIfExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (var paths = Files.walk(path)) {
            for (Path nested : paths.sorted((left, right) -> right.compareTo(left)).toList()) {
                Files.deleteIfExists(nested);
            }
        }
    }

    private static URL toUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not add " + file + " to the mod classpath.", exception);
        }
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
