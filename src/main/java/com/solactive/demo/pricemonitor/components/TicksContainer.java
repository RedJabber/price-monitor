package com.solactive.demo.pricemonitor.components;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;
import com.solactive.demo.pricemonitor.dto.TickDto;

/**
 * Container for ticks and stats.
 *
 * @author Andrey Arefyev
 */
public interface TicksContainer {

    AggregatedInfo getCommonStatistics();

    AggregatedInfo getAllTicksStatisticsByInstrument(String instrument);

    boolean put(TickDto tick);
}