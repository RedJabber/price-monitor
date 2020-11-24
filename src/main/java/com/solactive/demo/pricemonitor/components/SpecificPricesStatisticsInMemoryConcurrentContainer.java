package com.solactive.demo.pricemonitor.components;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;


import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * SpecificPricesStatisticsContainer.
 *
 * @author Andrey Arefyev
 */
@Component
public class SpecificPricesStatisticsInMemoryConcurrentContainer implements ConcurrentContainer<Map<String, PriceStatistics>>{

    private final Queue<Map<String, PriceStatistics>> stats = new ConcurrentLinkedQueue<>();
    {
        stats.add(Collections.emptyMap());
    }

    @Override
    public Map<String, PriceStatistics> get() {
        return stats.peek();
    }

    @Override
    public void set(Map<String, PriceStatistics> newValue) {
        stats.add(newValue);
        stats.poll();
    }

}