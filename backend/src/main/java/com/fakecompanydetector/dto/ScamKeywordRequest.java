package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.KeywordCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScamKeywordRequest {
    @NotBlank
    private String keyword;
    
    @NotNull
    private KeywordCategory category;
    
    @NotNull
    private Integer weight;
    
    private Boolean isActive = true;
}
