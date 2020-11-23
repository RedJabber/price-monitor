package com.solactive.demo.pricemonitor.components;

import static java.util.Optional.ofNullable;

import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.dto.Tick;
import com.solactive.demo.pricemonitor.utils.GeneralPriceStatisticsAggregator;
import com.solactive.demo.pricemonitor.utils.PriceStatisticsAggregator;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * In memory ticks container with scheduled data management.
 *
 * @author Andrey Arefyev
 */
@Slf4j
@Component
public class SelfAwareInMemoryTicksContainer implements TicksContainer {
    public static final PriceStatistics EMPTY_STATS = PriceStatistics.builder().build();
    private Queue<Map<String, PriceStatistics>> stats = new ConcurrentLinkedQueue<>();
    {
        stats.add(Collections.emptyMap());
    }

    private Queue<PriceStatistics> commonStats = new ConcurrentLinkedQueue<>();
    {
        commonStats.add(EMPTY_STATS);
    }
    private Queue<Tick> ticks = new ConcurrentLinkedQueue<>();
    private final long aggregationDuration;

    public SelfAwareInMemoryTicksContainer(@Value("${priceaggregator.durationInSeconds:60}") long aggregationDurationInSeconds) {
        this.aggregationDuration = TimeUnit.SECONDS.toMillis(aggregationDurationInSeconds);
    }

    @Override
    public PriceStatistics getCommonStatistics() {
        return ofNullable(commonStats.peek()).orElse(EMPTY_STATS);
    }

    @Override
    public PriceStatistics getAllTicksStatisticsByInstrument(String instrument) {
        return ofNullable(stats.peek())
                .map(m -> m.get(instrument))
                .orElse(EMPTY_STATS);
    }

    @Override
    public boolean put(Tick tick) {
        long rightNow = System.currentTimeMillis();
        long secondStartAligned = getThresholdStart(rightNow);
        if (tick.getTimestamp() < secondStartAligned - aggregationDuration) {
            return false;
        }
        //no data from future allowed.
        if (tick.getTimestamp() > rightNow) {
            return false;
        }

        return ticks.add(tick);
    }


    @Scheduled(cron = "${priceaggregator.recalculationSchedule}")
    synchronized void recalculate() {

        long threshold = getThresholdStart(System.currentTimeMillis());

        var newSpecificStatsSnapshot = recalculateStats4SpecificInstruments(threshold);
        var newCommonAggregation = recalculateGeneralStatistics(newSpecificStatsSnapshot);

        stats.add(newSpecificStatsSnapshot);
        stats.poll();
        commonStats.add(newCommonAggregation);
        commonStats.poll();

        ticks.removeIf(tickDto -> tickDto.getTimestamp() < threshold - aggregationDuration);

    }

    private PriceStatistics recalculateGeneralStatistics(Map<String, PriceStatistics> newSpecificStatsSnapshot) {
        return newSpecificStatsSnapshot.values()
                .stream()
                .map(GeneralPriceStatisticsAggregator::create)
                .reduce(GeneralPriceStatisticsAggregator::merge)
                .flatMap(GeneralPriceStatisticsAggregator::get)
                .orElse(EMPTY_STATS);
    }

    private Map<String, PriceStatistics> recalculateStats4SpecificInstruments(long threshold) {
        var specificStatistics = new HashMap<String, PriceStatisticsAggregator>();
        this.ticks.stream()
                .filter(tickDto -> tickDto.getTimestamp() > threshold - aggregationDuration)
                .filter(tickDto -> tickDto.getPrice() > 0.0)
                .forEach(tickDto -> {
                    double price = tickDto.getPrice();
                    specificStatistics.compute(tickDto.getInstrument(),
                            (instrumentName, value) ->
                                    ofNullable(value)
                                            .map(aggregatedInfo -> aggregatedInfo.inc()
                                                    .tryMax(price)
                                                    .tryMin(price)
                                                    .updateAvg(price))
                                            .orElseGet(() -> PriceStatisticsAggregator.create(price)));
                });

        return specificStatistics.entrySet()
                .stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().build()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }


    private long getThresholdStart(long millis) {
        int millisecondsInSecond = 1000;
        if (millis % millisecondsInSecond == 0) {
            return millis;
        }
        return millis - millis % millisecondsInSecond;
    }
}