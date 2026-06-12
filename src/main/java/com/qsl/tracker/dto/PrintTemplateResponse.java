package com.qsl.tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PrintTemplateResponse {

    private Long id;
    private String templateName;
    private String templateType;
    private String backgroundFileKey;
    private JsonNode configJson;
    private Boolean enabled;
    private Boolean isDefault;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
