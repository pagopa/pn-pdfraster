package it.pagopa.pn.pdfraster.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FontUtilsTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        System.setProperty("user.home", tempDir.toString());
    }

    @Test
    void testLoadFonts() throws IOException {
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


    @Test
    void testLoadFonts_directoryCreationFails() {
        File mockFile = mock(File.class);
        when(mockFile.exists()).thenReturn(false);
        when(mockFile.mkdirs()).thenReturn(false); // Simuliamo il fallimento della creazione della cartella

        System.setProperty("user.home", "/mock/home");

        PathMatchingResourcePatternResolver mockResolver = mock(PathMatchingResourcePatternResolver.class);
        Resource[] mockResources = new Resource[0]; // Nessuna risorsa

        try {
            when(mockResolver.getResources("classpath:fonts/*.ttf")).thenReturn(mockResources);

            FontUtils.loadFonts(); // Metodo statico da testare

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}