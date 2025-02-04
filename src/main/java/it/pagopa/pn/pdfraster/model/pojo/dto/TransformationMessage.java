package it.pagopa.pn.pdfraster.model.pojo.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformationMessage {

    String fileKey;
    String transformationType;
    String bucketName;
    String contentType;

    public TransformationMessage(String fileKey, String bucketName) {
        this.fileKey = fileKey;
        this.bucketName = bucketName;
    }
}