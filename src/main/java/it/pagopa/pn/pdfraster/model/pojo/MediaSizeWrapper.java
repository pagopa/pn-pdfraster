package it.pagopa.pn.pdfraster.model.pojo;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public enum MediaSizeWrapper {
    A0("A0",PDRectangle.A0),
    A1("A1",PDRectangle.A1),
    A2("A2",PDRectangle.A2),
    A3("A3",PDRectangle.A3),
    A4("A4",PDRectangle.A4),
    A5("A5",PDRectangle.A5),
    A6("A6",PDRectangle.A6),
    TABLOID("TABLOID",PDRectangle.TABLOID),
    LETTER("LETTER",PDRectangle.LETTER),
    LEGAL("LEGAL",PDRectangle.LEGAL);

    private String valueName;
    private PDRectangle pdRectangle;

    MediaSizeWrapper(String valueName,PDRectangle pdRectangle){
        this.pdRectangle = pdRectangle;
        this.valueName = valueName;
    }

    public static PDRectangle getMediaSize(String value){
        for(MediaSizeWrapper m : MediaSizeWrapper.values()){
            if(m.valueName.equals(value)){
                return m.pdRectangle;
            }
        }
        return null;
    }
}
