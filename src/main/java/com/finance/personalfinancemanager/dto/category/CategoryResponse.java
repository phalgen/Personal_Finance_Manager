package com.finance.personalfinancemanager.dto.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private String name;
    private String type;

    // CHANGED: Serialize as "custom" in JSON while keeping field name as isCustom in Java
    @JsonProperty("custom")
    private Boolean isCustom;
}