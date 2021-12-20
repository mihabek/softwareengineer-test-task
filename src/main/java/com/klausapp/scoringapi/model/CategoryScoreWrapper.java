package com.klausapp.scoringapi.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

import java.util.List;

import static java.util.Collections.emptyList;

@Getter
@Builder
public class CategoryScoreWrapper{
    @Default
    private List<CategoryScore> dailyScores = emptyList();
    @Default
    private List<CategoryScore> weeklyScores = emptyList();
}
