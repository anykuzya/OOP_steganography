package app;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.junit.Assert.assertEquals;

public class MainIntegTests {

    Path workingDirectory;

    @Test
    @Ignore
    public void name() throws Exception {
        String[] images = new String[]{"pal8rle.bmp", "rgb24pal.bmp", "rgb32.bmp"};
        Path sourceTextPath = this.workingDirectory.resolve("source_text");
        byte[] sourceTextBytes = "Gradile -- лучшая система сборки!".getBytes(StandardCharsets.UTF_8);
        Files.write(sourceTextPath, sourceTextBytes);

        for (String imageName : images) {
            Path imagePath = this.workingDirectory.resolve(imageName);
            Files.copy(getClass().getResourceAsStream("/sampleImages/" + imageName), imagePath);

            Path imageWithTextPath = this.workingDirectory.resolve("embedded_" + imageName);
            Main.main(
                    "--embed",
                    "-i", imagePath.toString(),
                    "-b", "2",
                    "-e", "UTF-8",
                    "-d", sourceTextPath.toString(),
                    "-o", imageWithTextPath.toString()
            );
            Path decodedTextPath = this.workingDirectory.resolve("decoded_text_" + imageName);
            Main.main("--decode", "-i", imageWithTextPath.toString(), "-o", decodedTextPath.toString());

            assertEquals(Files.readAllBytes(sourceTextPath), Files.readAllBytes(decodedTextPath));
        }
    }

    @Before
    public void setUp() throws Exception {
        this.workingDirectory = Files.createTempDirectory(null);
    }

    @After
    public void tearDown() throws Exception {
        Files.walkFileTree(this.workingDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }
}
