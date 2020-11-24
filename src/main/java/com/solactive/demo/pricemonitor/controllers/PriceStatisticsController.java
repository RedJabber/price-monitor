package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.services.TicksService;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * StatisticsController.
 *
 * @author Andrey Arefyev
 */
@Slf4j
@RestController
public class PriceStatisticsController implements PriceStatisticsApi {

    private final TicksService ticksService;

    public PriceStatisticsController(TicksService ticksService) {this.ticksService = ticksService;}

    @Override
    public PriceStatistics getLastMinuteStats() {
        return ticksService.getAllTicksStatistics();
    }

    @Override
    public PriceStatistics getLastMinuteStatsForInstrument(@PathVariable String instrument) {
        log.info("instrument is {}", instrument);
        return ticksService.getAllTicksStatisticsByInstrument(instrument);
    }
}