package com.solactive.demo.pricemonitor.services;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.dto.Tick;


import java.time.LocalDateTime;

/**
 * Service over the data aggregator.
 *
 * @author Andrey Arefyev
 */
public interface TicksService {

    PriceStatistics getAllTicksStatistics();

    PriceStatistics getAllTicksStatisticsByInstrument(String instrument);

    boolean tryTick(Tick tick, LocalDateTime now);
}