package com.solactive.demo.pricemonitor.components;

import static java.util.Optional.ofNullable;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;


import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * GeneralPriceStatisticsContainer.
 *
 * @author Andrey Arefyev
 */
@Component
public class PriceStatisticsInMemoryConcurrentContainer implements ConcurrentContainer<PriceStatistics>{
    public static final PriceStatistics EMPTY_STATS = PriceStatistics.builder().build();
    private final Queue<PriceStatistics> commonStats = new ConcurrentLinkedQueue<>();
    {
        commonStats.add(EMPTY_STATS);
    }

    public PriceStatistics get() {
        return ofNullable(commonStats.peek()).orElse(EMPTY_STATS);
    }


    public void set(PriceStatistics newCommonAggregation) {
        commonStats.add(newCommonAggregation);
        commonStats.poll();
    }
}