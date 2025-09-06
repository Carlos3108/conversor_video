package org.azdev.conversor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoUploadDTO {
    private String fileId;
    private String originalFileName;
    private String contentType;
    private long size;
    private String message;
}
