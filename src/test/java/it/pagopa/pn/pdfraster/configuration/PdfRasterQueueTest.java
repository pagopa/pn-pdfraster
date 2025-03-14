package it.pagopa.pn.pdfraster.configuration;


import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTestWebEnv
@CustomLog
@DirtiesContext
class PdfRasterQueueTest {

    @Value("${sqs.queue.transformation-raster-queue-name}")
    private String transformationRasterQueueName;

    @Test
    void testQueueName(){
        Assertions.assertNotNull(transformationRasterQueueName);
        Assertions.assertEquals("pn-ss-transformation-raster-queue", transformationRasterQueueName);
    }
}
