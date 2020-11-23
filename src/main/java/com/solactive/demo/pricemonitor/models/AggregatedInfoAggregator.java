package com.solactive.demo.pricemonitor.models;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;


import lombok.Builder;

import java.util.Optional;

/**
 * AggregatedInfoAggregator.
 *
 * @author Andrey Arefyev
 */
public class AggregatedInfoAggregator {
    private long counter;
    private double max;
    private double min;
    private double avg;

    @Builder
    private AggregatedInfoAggregator(long counter, double max, double min, double avg) {
        this.counter = counter;
        this.max = max;
        this.min = min;
        this.avg = avg;
    }

    public static AggregatedInfoAggregator create(AggregatedInfo aggregatedInfo) {
        return AggregatedInfoAggregator.builder()
                .avg(aggregatedInfo.getAvg())
                .counter(aggregatedInfo.getCount())
                .max(aggregatedInfo.getMax())
                .min(aggregatedInfo.getMin())
                .build();
    }

    public AggregatedInfoAggregator merge(AggregatedInfoAggregator that) {
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

    public Optional<AggregatedInfo> get() {
        if (counter == 0) {
            return Optional.empty();
        }

        var aggregatedInfo = AggregatedInfo.builder()
                .count(this.counter)
                .min(this.min)
                .max(this.max)
                .avg(this.avg)
                .build();
        return Optional.of(aggregatedInfo);
    }
}