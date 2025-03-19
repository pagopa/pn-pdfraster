package it.pagopa.pn.pdfraster.utils;

import it.pagopa.pn.pdfraster.utils.FontUtils;
import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTestWebEnv
class FontUtilsTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setup() {
        // Imposta la cartella temporanea come "user.home" per il test
        System.setProperty("user.home", tempDir.toString());
    }

    @Test
    public void testLoadFonts() throws IOException {
        FontUtils.loadFonts();

        Path fontDir = tempDir.resolve(".fonts");
        assertTrue(Files.exists(fontDir) && Files.isDirectory(fontDir), "La cartella dei font deve esistere");

        Path copiedFont = fontDir.resolve("arial.ttf");
        assertTrue(Files.exists(copiedFont), "Il font 'arial.ttf' deve essere stato copiato");

        try (InputStream originalStream = getClass().getResourceAsStream("/fonts/arial.ttf")) {
            assertNotNull(originalStream, "La risorsa originale 'arial.ttf' deve esistere nei test resources");
            byte[] originalBytes = originalStream.readAllBytes();
            byte[] copiedBytes = Files.readAllBytes(copiedFont);
            assertArrayEquals(originalBytes, copiedBytes, "Il contenuto del font copiato deve essere uguale a quello della risorsa originale");
        }
    }

}