package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;
import com.solactive.demo.pricemonitor.services.TicksService;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * StatisticsController.
 *
 * @author Andrey Arefyev
 */
@RestController
public class StatisticsController implements StatisticsApi {

    private final TicksService ticksService;

    public StatisticsController(TicksService ticksService) {this.ticksService = ticksService;}

    @Override
    public AggregatedInfo getLastMinuteStats() {
        return ticksService.getAllTicksStatistics();
    }

    @Override
    public AggregatedInfo getLastMinuteStatsForInstrument(@PathVariable String instrument) {
        return ticksService.getAllTicksStatisticsByInstrument(instrument);
    }
}