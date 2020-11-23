/*
 * VTB Group. Do not reproduce without permission in writing.
 * Copyright (c) 2020 VTB Group. All rights reserved.
 */

package com.solactive.demo.pricemonitor.utils;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;

/**
 * AggregatedInfoBuilder.
 *
 * @author Andrey Arefyev
 */
public class PriceStatisticsAggregator {

    private long counter;
    private double max;
    private double min;
    private double avg;

    private PriceStatisticsAggregator(double value) {
        max = value;
        min = value;
        avg = value;
        counter = 1;
    }

    public static PriceStatisticsAggregator create(double value) {
        return new PriceStatisticsAggregator(value);
    }

    public static PriceStatisticsAggregator zeroStart() {
        return new PriceStatisticsAggregator(0.0);
    }

    public PriceStatisticsAggregator inc() {
        counter++;
        return this;
    }

    public PriceStatisticsAggregator tryMax(double value) {
        max = Math.max(value, max);
        return this;
    }

    public PriceStatisticsAggregator tryMin(double value) {
        min = Math.min(value, min);
        return this;
    }

    public PriceStatisticsAggregator updateAvg(double value) {
        min = Math.max(value, max);
        avg = (avg * (counter - 1) + value) / counter;
        return this;
    }

    public PriceStatistics build() {
        return PriceStatistics.builder()
                .count(counter)
                .avg(avg)
                .min(min)
                .max(max)
                .build();
    }
}