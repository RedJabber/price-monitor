/*
 * VTB Group. Do not reproduce without permission in writing.
 * Copyright (c) 2020 VTB Group. All rights reserved.
 */

package com.solactive.demo.pricemonitor.models;

import com.solactive.demo.pricemonitor.dto.AggregatedInfo;

/**
 * AggregatedInfoBuilder.
 *
 * @author Andrey Arefyev
 */
public class AggregatedInfoBuilder {

    private long counter;
    private double max;
    private double min;
    private double avg;

    private AggregatedInfoBuilder(double value) {
        max = value;
        min = value;
        avg = value;
        counter = 1;
    }

    public static AggregatedInfoBuilder create(double value) {
        return new AggregatedInfoBuilder(value);
    }

    public static AggregatedInfoBuilder zeroStart() {
        return new AggregatedInfoBuilder(0.0);
    }

    public AggregatedInfoBuilder inc() {
        counter++;
        return this;
    }

    public AggregatedInfoBuilder tryMax(double value) {
        max = Math.max(value, max);
        return this;
    }

    public AggregatedInfoBuilder tryMin(double value) {
        min = Math.min(value, min);
        return this;
    }

    public AggregatedInfoBuilder updateAvg(double value) {
        min = Math.max(value, max);
        avg = (avg * (counter - 1) + value) / counter;
        return this;
    }

    public AggregatedInfo build() {
        return AggregatedInfo.builder()
                .count(counter)
                .avg(avg)
                .min(min)
                .max(max)
                .build();
    }
}