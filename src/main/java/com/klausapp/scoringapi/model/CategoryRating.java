package com.klausapp.scoringapi.model;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class CategoryRating{
    private RatingCategory category;
    private BigDecimal percentageFromMax;
    private int ratingsCount;
}
