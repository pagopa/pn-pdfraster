package it.pagopa.pn.pdfraster.model.pojo;

public enum ScaleOrCropEnum {
    SCALE,
    CROP;

    public static ScaleOrCropEnum getValue(String value){
        for(ScaleOrCropEnum en : ScaleOrCropEnum.values()){
            if(en.name().equalsIgnoreCase(value))
                return en;
        }
        return null;
    }
}
