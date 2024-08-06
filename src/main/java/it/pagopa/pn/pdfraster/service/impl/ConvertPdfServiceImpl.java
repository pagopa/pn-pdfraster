package it.pagopa.pn.pdfraster.service.impl;

import it.pagopa.pn.pdfraster.service.ConvertPdfService;
import lombok.CustomLog;
import org.springframework.stereotype.Service;

import java.io.OutputStream;

@CustomLog
@Service
public class ConvertPdfServiceImpl implements ConvertPdfService {



    @Override
    public OutputStream convertPdfToImage(byte[] file){
        return null;
    }
}
