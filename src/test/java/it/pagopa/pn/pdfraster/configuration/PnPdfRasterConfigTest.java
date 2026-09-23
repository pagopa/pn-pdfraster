package it.pagopa.pn.pdfraster.configuration;

import it.pagopa.pn.pdfraster.configuration.properties.PnPdfRasterConfig;
import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTestWebEnv
@DirtiesContext
class PnPdfRasterConfigTest {

    @Autowired(required = false)
    private PnPdfRasterConfig pnPdfRasterConfig;

    @Test
    void pnPdfRasterConfig_isLoaded() {
        assertNotNull(pnPdfRasterConfig, "PnPdfRasterConfig deve essere caricata come bean");
    }

    @Test
    void pnPdfRasterConfig_parameterName_isReadFromNewKey() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getParameterName(),
                "pn.pdfraster.parameter-name deve avere un valore");
        assertEquals("pn-PDFRaster", pnPdfRasterConfig.getParameterName());
    }

    @Test
    void pnPdfRasterConfig_maxThreadPoolSize_isRead() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getMaxThreadPoolSize());
        assertEquals(100, pnPdfRasterConfig.getMaxThreadPoolSize());
    }

    @Test
    void pnPdfRasterConfig_maxTransformationRetry_isRead() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getMaxTransformationRetry());
        assertEquals(10, pnPdfRasterConfig.getMaxTransformationRetry());
    }

    @Test
    void pnPdfRasterConfig_sqs_isRead() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getSqs());
    }

    @Test
    void pnPdfRasterConfig_sqs_transformationQueueName_isReadFromNewKey() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getSqs());
        assertNotNull(pnPdfRasterConfig.getSqs().getTransformationQueueName(),
                "pn.pdfraster.sqs.transformation-queue-name deve avere un valore");
        assertEquals("pn-ss-transformation-raster-queue",
                pnPdfRasterConfig.getSqs().getTransformationQueueName());
    }

    @Test
    void pnPdfRasterConfig_sqs_maxMessages_isRead() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getSqs());
        assertNotNull(pnPdfRasterConfig.getSqs().getMaxMessages());
        assertEquals(10, pnPdfRasterConfig.getSqs().getMaxMessages());
    }

    @Test
    void pnPdfRasterConfig_sqs_retryStrategy_isRead() {
        assertNotNull(pnPdfRasterConfig);
        assertNotNull(pnPdfRasterConfig.getSqs());
        assertNotNull(pnPdfRasterConfig.getSqs().getRetryStrategy());
        assertEquals(3L, pnPdfRasterConfig.getSqs().getRetryStrategy().getMaxAttempts());
        assertEquals(3L, pnPdfRasterConfig.getSqs().getRetryStrategy().getMinBackoff());
    }
}
