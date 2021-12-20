package com.klausapp.scoringapi.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class Rating{
    private Long id;
    private int rating;
    private Long ticketId;
    private Long ratingCategoryId;
    private Long reviewerId;
    private Long revieweeId;
    private LocalDateTime createdAt;
}
