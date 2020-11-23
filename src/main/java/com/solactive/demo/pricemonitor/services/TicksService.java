package com.solactive.demo.pricemonitor.services;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;
import com.solactive.demo.pricemonitor.dto.TickDto;


import java.time.LocalDateTime;

/**
 * Service over the data aggregator.
 *
 * @author Andrey Arefyev
 */
public interface TicksService {

    AggregatedInfo getAllTicksStatistics();

    AggregatedInfo getAllTicksStatisticsByInstrument(String instrument);

    boolean tryTick(TickDto tick, LocalDateTime now);
}