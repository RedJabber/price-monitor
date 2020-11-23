package com.solactive.demo.pricemonitor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Getter;

/**
 * TickDao.
 * <pre>{@code {
 *  "instrument": "IBM.N",
 *  "price": 143.82,
 *  "timestamp": 1478192204000
 * }}
 * </pre>
 *
 * @author Andrey Arefyev
 */
@Getter
public class TickDto {
    /**
     * a financial instrument identifier
      */
    private final String instrument;

    /**
     * current trade price of a financial instrument (double)
     */
    private final double price;

    /**
     * timestamp in milliseconds
     */
    private final long timestamp;

    public TickDto(
            @JsonProperty("instrument") String instrument,
            @JsonProperty("price") double price,
            @JsonProperty("timestamp") long timestamp) {
        this.instrument = instrument;
        this.price = price;
        this.timestamp = timestamp;
    }

}