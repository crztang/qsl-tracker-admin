package com.qsl.tracker.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PrintTemplateRequest {

    private Long id;
    @NotBlank
    @Size(max = 128)
    private String templateName;
    @NotNull
    @Pattern(regexp = "[12]")
    private String templateType;
    private String backgroundFileKey;
    @NotNull
    private JsonNode configJson;
    private Boolean enabled = true;
    private Boolean isDefault = false;
    private Integer sortOrder = 0;
    @Size(max = 500)
    private String remark;
}
