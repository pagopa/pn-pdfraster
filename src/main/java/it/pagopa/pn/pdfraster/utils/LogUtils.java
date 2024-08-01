package it.pagopa.pn.pdfraster.utils;

public abstract class LogUtils {

    private LogUtils() {
        throw new IllegalStateException("LogUtils is a utility class");
    }

    // METODO API
    public static final String MDC_CORR_ID_KEY = "cx_id";
    public static final String CONVERT_PDF = "convertPdf";

    //#####SQS#####
    public static final String SQS_SEND = "send";

    //#####SAFE STORAGE#####
    public static final String SAFE_STORAGE_SERVICE = "pn-safestorage";
    public static final String GET_FILE = "getFile";
    public static final String POST_FILE = "postFile";

}
