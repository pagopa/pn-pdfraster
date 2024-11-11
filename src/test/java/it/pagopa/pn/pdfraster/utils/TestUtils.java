package it.pagopa.pn.pdfraster.utils;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;

public abstract class TestUtils {

    private TestUtils(){
    }

    /**
     * Metodo per recuperare il file di test dalle risorse
     * @return
     */
    public static byte[] getFileTestFromResources(){
        try (var in = new FileInputStream("src/test/resources/TEST.pdf")){
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo per recuperare il file di test dalle risorse
     * @return
     */
    public static byte[] getFileKoTestFromResources(){
        try (var in = new FileInputStream("src/test/resources/TEST_KO.pdf")){
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getFileOkLandScapeFromResources(){
        try (var in = new FileInputStream("src/test/resources/TEST_LANDSCAPE.pdf")){
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getFileOkPortraitFromResources() {
        try (var in = new FileInputStream("src/test/resources/TEST_PORTRAIT.pdf")){
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
