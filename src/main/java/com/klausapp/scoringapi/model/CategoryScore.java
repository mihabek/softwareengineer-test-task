package com.klausapp.scoringapi.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Builder(toBuilder = true)
public class CategoryScore{
    private String date;
    private BigDecimal score;
    @Default
    private List<CategoryRating> categoryRatingList = emptyList();
}
