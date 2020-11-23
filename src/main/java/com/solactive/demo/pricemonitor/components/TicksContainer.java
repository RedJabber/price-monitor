package com.solactive.demo.pricemonitor.components;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.dto.Tick;

/**
 * Container for ticks and stats.
 *
 * @author Andrey Arefyev
 */
public interface TicksContainer {

    PriceStatistics getCommonStatistics();

    PriceStatistics getAllTicksStatisticsByInstrument(String instrument);

    boolean put(Tick tick);
}