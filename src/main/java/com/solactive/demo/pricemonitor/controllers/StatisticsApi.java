package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;


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
@RequestMapping(StatisticsApi.STATISTICS_ROOT)
public interface StatisticsApi {
    String STATISTICS_ROOT = "/statistics";

    /**
     * This is the endpoint with aggregated statistics for all ticks across all instruments.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    AggregatedInfo getLastMinuteStats();


    /**
     * This is the endpoint with statistics for a given instrument.
     * @param instrument name on the instrument what it was registered during the tick.
     * @return instrument specific statistics in a last interval.
     */
    @GetMapping("/{instrument}")
    @ResponseStatus(HttpStatus.OK)
    AggregatedInfo getLastMinuteStatsForInstrument(@PathVariable String instrument);


}