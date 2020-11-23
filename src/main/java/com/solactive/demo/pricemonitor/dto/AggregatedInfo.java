package com.solactive.demo.pricemonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * AggregatedInfo.
 * <pre>{@code {
 *  "avg": 100,
 *  "max": 200,
 *  "min": 50,
 *  "count": 10
 * }}
 * </pre>
 *
 * @author Andrey Arefyev
 */

@Builder
@Getter
@ToString
public class AggregatedInfo {

    /**
     * avg is a double specifying the average amount of all tick prices in the last 60 seconds.
     */
    @JsonProperty
    private final Double avg;

    /**
     * max is a double specifying single highest tick price in the last 60 seconds.
     */
    @JsonProperty
    private final Double max;

    /**
     *  min is a double specifying single lowest tick price in the last 60 seconds.
     */
    @JsonProperty
    private final Double min;

    /**
     * count is a long specifying the total number of ticks happened in the last 60 seconds.
     */
    @JsonProperty
    private final long count;

}