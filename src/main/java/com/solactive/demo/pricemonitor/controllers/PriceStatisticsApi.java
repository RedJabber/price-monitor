package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * StatisticsApi.
 *
 * @author Andrey Arefyev
 */
@RequestMapping(PriceStatisticsApi.STATISTICS_ROOT)
public interface PriceStatisticsApi {
    String STATISTICS_ROOT = "/statistics";

    /**
     * This is the endpoint with aggregated statistics for all ticks across all instruments.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(tags = "price-statistics")
    PriceStatistics getLastMinuteStats();


    /**
     * This is the endpoint with statistics for a given instrument.
     * @param instrument name on the instrument what it was registered during the tick.
     * @return instrument specific statistics in a last interval.
     */
    @GetMapping("/{instrument}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(tags = "price-statistics", parameters = @Parameter(name = "instrument", description = "Instrument name."))
    PriceStatistics getLastMinuteStatsForInstrument(@PathVariable String instrument);


}