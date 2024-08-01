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
    public static final String INSERTING_DATA_IN_SQS = "Inserting data {} in SQS '{}'";
    public static final String INSERTED_DATA_IN_SQS = "Inserted data in SQS '{}'";
    public static final String SHORT_RETRY_ATTEMPT = "Short retry attempt number '{}' caused by : {} - {}";


    //#####SAFE STORAGE#####
    public static final String SAFE_STORAGE_SERVICE = "pn-safestorage";
    public static final String GET_FILE = "getFile";
    public static final String POST_FILE = "postFile";

}
