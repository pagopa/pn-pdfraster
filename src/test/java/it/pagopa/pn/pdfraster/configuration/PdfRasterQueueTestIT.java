package it.pagopa.pn.pdfraster.configuration;

import it.pagopa.pn.pdfraster.exceptions.SqsClientException;
import it.pagopa.pn.pdfraster.utils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTestWebEnv
@CustomLog
@DirtiesContext
class PdfRasterQueueTestIT {

    @Value("${sqs.queue.transformation-raster-queue-name}")
    private String transformationRasterQueueName;

    @Test
    void testQueueName(){
        Assertions.assertNotNull(transformationRasterQueueName);
        assertEquals("pn-ss-transformation-raster-queue", transformationRasterQueueName);
    }

    @Test
    void testSqsClientExceptionMessage() {
        String queueName = "testQueue";
        SqsClientException exception = new SqsClientException(queueName);

        String expectedMessage = "An error occurred during client operation on 'testQueue' queue";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSqsClientExceptionIsRuntimeException() {
        SqsClientException exception = new SqsClientException("queue");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptionMessage() {
        String queueName = "test-queue";
        SqsClientException exception = assertThrows(
                SqsClientException.class,
                () -> { throw new SqsClientException(queueName); }
                                                   );

        assertEquals("An error occurred during client operation on 'test-queue' queue", exception.getMessage());
    }

}
