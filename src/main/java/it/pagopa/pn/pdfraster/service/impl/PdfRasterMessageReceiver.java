package it.pagopa.pn.pdfraster.service.impl;

import io.awspring.cloud.messaging.listener.Acknowledgment;
import it.pagopa.pn.pdfraster.model.pojo.DocumentQueueDto;
import lombok.CustomLog;
import org.springframework.stereotype.Service;

@CustomLog
@Service
public class PdfRasterMessageReceiver {


//    @SqsListener(value = "${pn.pdfraster.sqs.queue.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void lavorazionePdfRasterDocuments(final DocumentQueueDto documentQueueDto, final Acknowledgment acknowledgment) {
        // da completare
    }
}
