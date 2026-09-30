package com.fakecompanydetector.dto;

import com.fakecompanydetector.entity.enums.KeywordCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScamKeywordDto {
    private UUID id;
    private String keyword;
    private KeywordCategory category;
    private Integer weight;
    private Boolean isActive;
}
