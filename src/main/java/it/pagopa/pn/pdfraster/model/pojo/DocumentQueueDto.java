package it.pagopa.pn.pdfraster.model.pojo;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
@Builder
public class DocumentQueueDto {

    String uploadUrl;
    String fileKey;
    String secret;

}
