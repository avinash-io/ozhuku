package io.github.avinashio.ozhuku.acquisition.pull;

import io.github.avinashio.ozhuku.acquisition.ScannedResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileSourceScannerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldFindFilesInDirectory() throws IOException {

        Files.createFile(tempDir.resolve("file1.txt"));
        Files.createFile(tempDir.resolve("file2.csv"));
        Files.createDirectories(tempDir.resolve("subdir"));
        Files.createFile(tempDir.resolve("subdir/file3.txt"));

        final FileSourceScanner scanner =
                new FileSourceScanner(tempDir);

        final List<ScannedResource> resources = scanner.scan();

        assertEquals(3, resources.size());
    }

    @Test
    void shouldReturnEmptyListForNonExistentDirectory() throws IOException {

        final Path nonExistent =
                tempDir.resolve("does-not-exist");

        final FileSourceScanner scanner =
                new FileSourceScanner(nonExistent);

        final List<ScannedResource> resources = scanner.scan();

        assertTrue(resources.isEmpty());
    }

    @Test
    void shouldSetResourceMetadata() throws IOException {

        final Path file = tempDir.resolve("data.txt");
        Files.write(file, "hello".getBytes());

        final FileSourceScanner scanner =
                new FileSourceScanner(tempDir);

        final List<ScannedResource> resources = scanner.scan();

        assertEquals(1, resources.size());

        final ScannedResource resource = resources.get(0);
        assertNotNull(resource.resourceId());
        assertNotNull(resource.location());
        assertTrue(resource.size() > 0);
        assertTrue(resource.lastModified() > 0);
    }

    @Test
    void shouldScanSingleFile() throws IOException {

        Files.createFile(tempDir.resolve("only.txt"));

        final FileSourceScanner scanner =
                new FileSourceScanner(tempDir);

        final List<ScannedResource> resources = scanner.scan();

        assertEquals(1, resources.size());

        final ScannedResource resource = resources.get(0);
        assertNotNull(resource.resourceId());
        assertTrue(resource.resourceId().value().contains("only.txt"));
    }
}
