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
public class SelfAwareInMemoryTicksContainer implements TicksContainer, Recalculatable {
    public static final PriceStatistics EMPTY_STATS = PriceStatistics.builder().build();

    private final ConcurrentContainer<PriceStatistics> generalPriceStatisticsContainer;
    private final ConcurrentContainer<Map<String, PriceStatistics>> specificPricesStatisticsContainer;

    private final Queue<Tick> ticks = new ConcurrentLinkedQueue<>();

    private final long aggregationDuration;
    private final TimeStrategy timeStrategy;

    public SelfAwareInMemoryTicksContainer(
            @Value("${priceaggregator.durationInSeconds:60}") long aggregationDurationInSeconds,
            ConcurrentContainer<PriceStatistics> generalPriceStatisticsContainer,
            ConcurrentContainer<Map<String, PriceStatistics>> specificPricesStatisticsContainer,
            TimeStrategy timeStrategy) {
        this.generalPriceStatisticsContainer = generalPriceStatisticsContainer;
        this.aggregationDuration = TimeUnit.SECONDS.toMillis(aggregationDurationInSeconds);
        this.specificPricesStatisticsContainer = specificPricesStatisticsContainer;
        this.timeStrategy = timeStrategy;
    }

    @Override
    public PriceStatistics getCommonStatistics() {
        return generalPriceStatisticsContainer.get();
    }

    @Override
    public PriceStatistics getAllTicksStatisticsByInstrument(String instrument) {
        return ofNullable(specificPricesStatisticsContainer.get())
                .map(m -> m.get(instrument))
                .orElse(EMPTY_STATS);
    }

    @Override
    public boolean put(Tick tick) {
        long rightNow = timeStrategy.getCurrentTimestampMillis();
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


    @Override
    @Scheduled(cron = "${priceaggregator.recalculationSchedule}")
    public synchronized void recalculate() {

        long threshold = getThresholdStart(timeStrategy.getCurrentTimestampMillis());

        var newSpecificStatsSnapshot = recalculateStats4SpecificInstruments(threshold);
        var newCommonAggregation = recalculateGeneralStatistics(newSpecificStatsSnapshot);

        specificPricesStatisticsContainer.set(newSpecificStatsSnapshot);
        generalPriceStatisticsContainer.set(newCommonAggregation);

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