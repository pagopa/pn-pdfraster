package it.pagopa.pn.pdfraster.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class FontProvider {

    public static void loadFonts(PDDocument pdDocument) {
        List<PDType0Font> embeddedFontList = new ArrayList<>();
        String fontPath = "src/main/resources/fonts";
        File fontDirectory = new File(fontPath);
        File[] fonts = fontDirectory.listFiles();
        log.info("Percorso font: {}", new File(fontPath).getAbsolutePath());

        if (fonts != null && fonts.length > 0) {
            log.info("Sono stati trovati dei font");
            for (File f : fonts) {
                try {
                    PDType0Font font = PDType0Font.load(pdDocument, f);
                    embeddedFontList.add(font);
                } catch (IOException e) {
                    log.error("Errore nel caricamento del font: {}", f.getName(), e);
                }
            }
        } else {
            log.info("Nessun font trovato");
        }

    }
}
