package com.spring.be.blogReview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStatsDto {

    @JsonProperty("1")
    private int one;

    @JsonProperty("2")
    private int two;

    @JsonProperty("3")
    private int three;

    @JsonProperty("4")
    private int four;

    @JsonProperty("5")
    private int five;
}