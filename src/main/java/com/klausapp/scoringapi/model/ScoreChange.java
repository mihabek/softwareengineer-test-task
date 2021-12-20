package com.klausapp.scoringapi.model;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ScoreChange{
    private BigDecimal previousScore;
    private BigDecimal selectedScore;
    private BigDecimal difference;
}
