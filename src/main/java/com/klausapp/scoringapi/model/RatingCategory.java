package com.klausapp.scoringapi.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingCategory{
    private Long id;
    private String name;
    private double weight;
}
