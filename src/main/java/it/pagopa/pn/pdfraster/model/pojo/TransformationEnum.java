package it.pagopa.pn.pdfraster.model.pojo;

public enum TransformationEnum {
    SCALE,
    CROP,
    PORTRAIT;

    public static TransformationEnum getValue(String value){
        for(TransformationEnum en : TransformationEnum.values()){
            if(en.name().equalsIgnoreCase(value))
                return en;
        }
        return null;
    }
}
