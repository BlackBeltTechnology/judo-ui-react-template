package hu.blackbelt.judo.ui.generator.react;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Generated browser-storage keys must not be derived from the path the application is served on.
 * Guards against re-introducing the withdrawn storage-namespace helper.
 */
public class NoStorageNamespacingTest {
    @Test
    void templatesContainNoStorageNamespacing() throws IOException {
        Path resources = Path.of("src/main/resources");
        try (Stream<Path> files = Files.walk(resources)) {
            List<String> offenders = files
                    .filter(Files::isRegularFile)
                    .filter(NoStorageNamespacingTest::containsNamespacing)
                    .map(Path::toString)
                    .toList();
            assertTrue(offenders.isEmpty(), "Storage namespacing must not be re-introduced: " + offenders);
        }
    }

    private static boolean containsNamespacing(Path path) {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return content.contains("storage-namespace") || content.contains("namespacedStorageKey")
                    || content.contains("clearNamespacedStorage");
        } catch (IOException e) {
            return false; // non-text resource
        }
    }
}
