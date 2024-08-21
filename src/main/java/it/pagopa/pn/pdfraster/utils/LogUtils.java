package it.pagopa.pn.pdfraster.utils;

public abstract class LogUtils {

    private LogUtils() {
        throw new IllegalStateException("LogUtils is a utility class");
    }

    // METODO API
    public static final String MDC_CORR_ID_KEY = "cx_id";
    public static final String CONVERT_PDF = "PdfRasterServiceImpl.convertPdf";

    public static final String INVALID_REQUEST = "Invalid Request";

    public static final String SUCCESSFUL_OPERATION_ON_LABEL = "Successful operation on '{}' : '{}' = {}";
    public static final String SUCCESSFUL_OPERATION_NO_RESULT_LABEL = "Successful operation: '{}'";

    public static final String INVOKING_OPERATION_LABEL_WITH_ARGS = "Invoking operation '{}' with args: {}";
    public static final String INVOKING_OPERATION_LABEL = "Invoking operation '{}'";

    //#####SERVIZI ESTERNI#####
    public static final String CLIENT_METHOD_INVOCATION_WITH_ARGS = "Client method {} - args: {}";


}
