package it.pagopa.pn.pdfraster.utils;

public abstract class LogUtils {

    private LogUtils() {
        throw new IllegalStateException("LogUtils is a utility class");
    }

    // METODO API
    public static final String MDC_CORR_ID_KEY = "cx_id";
    public static final String CONVERT_PDF = "PdfRasterServiceImpl.convertPdf";

    // METODI PDF BOX
    public static final String PROCESS_PAGE = "ConvertPdfServiceImpl.processPage";
    public static final String CONVERT_PDF_TO_IMAGE = "ConvertPdfServiceImpl.convertPdfToImage";
    public static final String PAGE_INDEX = "Page Index: ";


    // ERROR
    public static final String ENDING_PROCESS_WITH_ERROR = "Ending '{}' Process with error = '{}' - '{}'";
    public static final String INVALID_REQUEST = "Invalid Request";

    public static final String SUCCESSFUL_OPERATION_ON_LABEL = "Successful operation on '{}' : '{}' = {}";
    public static final String SUCCESSFUL_OPERATION_NO_RESULT_LABEL = "Successful operation: '{}'";
    public static final String EXCEPTION_IN_PROCESS = "Exception in '{}'";

    public static final String INVOKING_OPERATION_LABEL = "Invoking operation '{}'";

    //SERVIZI ESTERNI
    public static final String CLIENT_METHOD_INVOCATION_WITH_ARGS = "Client method {} - args: {}";

    //S3
    public static final String GET_OBJECT = "getObject()";
    public static final String PUT_OBJECT = "putObject()";
    public static final String GET_OBJECT_TAGGING = "getObjectTagging()";
    public static final String PUT_OBJECT_TAGGING = "putObjectTagging()";

    //LABELS
    public static final String CLIENT_METHOD_RETURN = "Return client method: {} = {}";
    public static final String CLIENT_METHOD_RETURN_WITH_ERROR = "Return client method '{}' with error: {} - {}";

    public static final String RECEIVE_MESSAGE = "PdfRasterService.receiveMessage()";
    public static final String PROCESS_MESSAGE = "PdfRasterService.processMessage()";
    public static final String RECEIVE_TRANSFORMATION_MESSAGES = "receiveTransformationMessages()";
    public static final String SHORT_RETRY_ATTEMPT = "Short retry attempt number '{}' caused by : {} - {}";






}
