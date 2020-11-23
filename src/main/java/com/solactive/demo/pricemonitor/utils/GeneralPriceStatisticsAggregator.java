package com.solactive.demo.pricemonitor.utils;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;


import lombok.Builder;

import java.util.Optional;

/**
 * AggregatedInfoAggregator.
 *
 * @author Andrey Arefyev
 */
public class GeneralPriceStatisticsAggregator {
    private long counter;
    private double max;
    private double min;
    private double avg;

    @Builder
    private GeneralPriceStatisticsAggregator(long counter, double max, double min, double avg) {
        this.counter = counter;
        this.max = max;
        this.min = min;
        this.avg = avg;
    }

    public static GeneralPriceStatisticsAggregator create(PriceStatistics aggregatedInfo) {
        return GeneralPriceStatisticsAggregator.builder()
                .avg(aggregatedInfo.getAvg())
                .counter(aggregatedInfo.getCount())
                .max(aggregatedInfo.getMax())
                .min(aggregatedInfo.getMin())
                .build();
    }

    public GeneralPriceStatisticsAggregator merge(GeneralPriceStatisticsAggregator that) {
        if (that.counter == 0) {
            return this;
        }
        if (this.counter == 0) {
            return that;
        }

        this.max = Math.max(this.max, that.max);
        this.min = Math.min(this.min, that.min);
        this.avg = (this.avg * this.counter + that.avg * that.counter) / (this.counter + that.counter);
        this.counter += that.counter;

        return this;
    }

    public Optional<PriceStatistics> get() {
        if (counter == 0) {
            return Optional.empty();
        }

        var aggregatedInfo = PriceStatistics.builder()
                .count(this.counter)
                .min(this.min)
                .max(this.max)
                .avg(this.avg)
                .build();
        return Optional.of(aggregatedInfo);
    }
}