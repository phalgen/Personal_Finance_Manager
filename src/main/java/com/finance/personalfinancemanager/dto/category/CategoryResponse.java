package com.finance.personalfinancemanager.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private String name;
    private String type;
    private Boolean isCustom;
}
