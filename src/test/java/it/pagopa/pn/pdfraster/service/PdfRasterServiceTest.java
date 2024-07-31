package it.pagopa.pn.pdfraster.service;

import it.pagopa.pn.pdfraster.model.pojo.SqsMessageWrapper;
import it.pagopa.pn.pdfraster.pdfraster.rest.v1.dto.PdfRasterResponse;
import it.pagopa.pn.pdfraster.rest.call.SafeStorageCall;
import it.pagopa.pn.pdfraster.testutils.annotation.SpringBootTestWebEnv;
import lombok.CustomLog;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import static org.mockito.ArgumentMatchers.any;
import static reactor.core.publisher.Mono.when;
import static org.mockito.Mockito.*;


@SpringBootTestWebEnv
@CustomLog
public class PdfRasterServiceTest {


}
