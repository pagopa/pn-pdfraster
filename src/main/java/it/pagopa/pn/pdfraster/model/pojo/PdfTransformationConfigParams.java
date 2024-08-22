package it.pagopa.pn.pdfraster.model.pojo;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@NoArgsConstructor
public class PdfTransformationConfigParams {
    private String cropbox;
    private Number dpi;
    private String mediaSize;
    private String margins;
    private String scaleOrCrop;
    private Number maxFileSize;
}
