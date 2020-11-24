package com.solactive.demo.pricemonitor.controllers;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.solactive.demo.pricemonitor.components.Recalculatable;
import com.solactive.demo.pricemonitor.components.TimeStrategy;
import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.dto.Tick;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @see PriceStatisticsApi#getLastMinuteStats()
 * @see PriceStatisticsApi#getLastMinuteStatsForInstrument(String)
 * @see TickApi
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class PriceStatisticsApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TimeStrategy timeStrategy;


    @Autowired
    private Recalculatable ticksContainer;

    @Value("${priceaggregator.durationInSeconds}")
    private long acceptedDurationInSeconds;

    @Test
    @DirtiesContext
    void testActualTickRespondsCreatedAndAdded2Statistics() {
        when(timeStrategy.getCurrentTimestampMillis()).thenAnswer(inv -> currentTimeMillis());

        double price = RandomUtils.nextDouble();
        String instrument = randomAlphanumeric(5);
        var tickRequestBody = Map.of(
                "instrument", instrument,
                "price", price,
                "timestamp", currentTimeMillis());
        var postTickResponse = postTick(tickRequestBody);
        assertThat(postTickResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ticksContainer.recalculate();

        var statisticsResponse = getStatistics();
        assertThat(statisticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statisticsResponse.getBody()).isNotNull();
        assertThat(statisticsResponse.getBody().getCount()).isEqualTo(1);
        assertThat(statisticsResponse.getBody().getAvg()).isEqualTo(price);
        assertThat(statisticsResponse.getBody().getMin()).isEqualTo(price);
        assertThat(statisticsResponse.getBody().getMax()).isEqualTo(price);

        var specificStatisticsResponse = getSpecificStatistics(instrument);
        assertThat(specificStatisticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(specificStatisticsResponse.getBody()).isNotNull();
        assertThat(specificStatisticsResponse.getBody().getCount()).isEqualTo(1);
        assertThat(specificStatisticsResponse.getBody().getAvg()).isEqualTo(price);
        assertThat(specificStatisticsResponse.getBody().getMin()).isEqualTo(price);
        assertThat(specificStatisticsResponse.getBody().getMax()).isEqualTo(price);
    }

    @Test
    @DirtiesContext
    void testOutdatedTickRespondsNoContextAndSkipped() {
        when(timeStrategy.getCurrentTimestampMillis()).thenAnswer(inv -> currentTimeMillis());

        double price = RandomUtils.nextDouble();
        String skippedInstrument = randomAlphanumeric(6);

        long timestampInAcceptedRange = currentTimeMillis() - RandomUtils.nextLong(0, (acceptedDurationInSeconds - 1) * 1000);
        var acceptableTick = new Tick(randomAlphanumeric(6), price, timestampInAcceptedRange);
        postTick(acceptableTick);

        long outdatedTimestamp = currentTimeMillis() - (acceptedDurationInSeconds + 1) * 1000;
        var postTickResponse = postTick(new Tick(skippedInstrument, RandomUtils.nextDouble(),
                outdatedTimestamp));
        assertThat(postTickResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ticksContainer.recalculate();

        var statisticsResponse = getStatistics();
        assertThat(statisticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statisticsResponse.getBody()).isNotNull();
        assertThat(statisticsResponse.getBody().getCount()).isEqualTo(1);
        assertThat(statisticsResponse.getBody().getAvg()).isEqualTo(price);

        var specificStatisticsResponse = getSpecificStatistics(skippedInstrument);
        assertThat(specificStatisticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(specificStatisticsResponse.getBody()).isNotNull();
        assertThat(specificStatisticsResponse.getBody().getCount()).isZero();
        assertThat(specificStatisticsResponse.getBody().getAvg()).isNull();
    }

    @Test
    @DirtiesContext
    void testStatistCalculatedAndResetDuringTime() {

        var nowTimestamp = new AtomicLong(currentTimeMillis() - SECONDS.toMillis(2));

        when(timeStrategy.getCurrentTimestampMillis()).thenAnswer(inv -> nowTimestamp.get());

        double price1 = RandomUtils.nextDouble();
        double price2 = RandomUtils.nextDouble();
        long timestampInAcceptedRange = nowTimestamp.get() - RandomUtils.nextLong(2, (acceptedDurationInSeconds - 2) * 1000);
        String instrument0 = randomAlphanumeric(6);
        postTick(new Tick(instrument0, price1, timestampInAcceptedRange));
        postTick(new Tick(instrument0, price2, timestampInAcceptedRange + 1));

        long borderTimestamp = nowTimestamp.get() - (acceptedDurationInSeconds - 1) * 1000;
        double price0 = RandomUtils.nextDouble();
        postTick(new Tick(randomAlphanumeric(6), price0, borderTimestamp));

        ticksContainer.recalculate();
        double minInstrument0Price = min(price1, price2);
        double maxInstrument0Price = max(price1, price2);
        log.info("Prices {} {} {}", price0, price1, price2);
        var specificStatisticsResponse = getSpecificStatistics(instrument0);
        assertThat(specificStatisticsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(specificStatisticsResponse.getBody()).isNotNull();
        assertThat(specificStatisticsResponse.getBody().getCount()).isEqualTo(2);
        assertThat(specificStatisticsResponse.getBody().getAvg()).isEqualTo((price1 + price2)/2);
        assertThat(specificStatisticsResponse.getBody().getMin()).isEqualTo(minInstrument0Price);
        assertThat(specificStatisticsResponse.getBody().getMax()).isEqualTo(maxInstrument0Price);

        var statisticsResponseInterval0 = getStatistics();
        assertThat(statisticsResponseInterval0.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statisticsResponseInterval0.getBody()).isNotNull();
        assertThat(statisticsResponseInterval0.getBody().getCount()).isEqualTo(3);
        assertThat(statisticsResponseInterval0.getBody().getAvg()).isEqualTo((price2 + price1 + price0)/3);
        assertThat(statisticsResponseInterval0.getBody().getMin()).isEqualTo(min(minInstrument0Price, price0));
        assertThat(statisticsResponseInterval0.getBody().getMax()).isEqualTo(max(maxInstrument0Price, price0));

        nowTimestamp.getAndAdd(SECONDS.toMillis(2));

        ticksContainer.recalculate();

        var statisticsResponseInterval1 = getStatistics();
        assertThat(statisticsResponseInterval1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(statisticsResponseInterval1.getBody()).isNotNull();
        assertThat(statisticsResponseInterval1.getBody().getCount()).isEqualTo(2);
        assertThat(statisticsResponseInterval1.getBody().getAvg()).isEqualTo((price2 + price1)/2);
        assertThat(statisticsResponseInterval1.getBody().getMin()).isEqualTo(minInstrument0Price);
        assertThat(statisticsResponseInterval1.getBody().getMax()).isEqualTo(maxInstrument0Price);

    }

    @Test
    @DirtiesContext
    void testCalcMassAggregation() {
        int pricesLength = 100;

        var prices = new ArrayList<Double>(pricesLength);
        for (int i = 0; i < pricesLength; i++) {
            prices.add(RandomUtils.nextDouble());
        }
        when(timeStrategy.getCurrentTimestampMillis()).thenAnswer(inv -> currentTimeMillis());

        String instrument = randomAlphanumeric(6);
        int instrument0Count = RandomUtils.nextInt(pricesLength/2, pricesLength);
        var specificPrices = prices.subList(0, instrument0Count);
        fillSpecificInstrumentTicks(instrument, specificPrices);
        fillOtherTicks(prices, instrument0Count);

        var generalAvg = prices.stream().reduce(Double::sum).map(v->v/pricesLength).get();

        ticksContainer.recalculate();

        var statisticsResponse = getStatistics();
        assertThat(statisticsResponse.getBody()).isNotNull();
        assertThat(statisticsResponse.getBody().getCount()).isEqualTo(pricesLength);
        assertThat(statisticsResponse.getBody().getAvg()).isEqualTo(generalAvg);
        assertThat(statisticsResponse.getBody().getMin())
                .isEqualTo(prices.stream().min(Double::compareTo).get());
        assertThat(statisticsResponse.getBody().getMax())
                .isEqualTo(prices.stream().max(Double::compareTo).get());

        var specificAvg = specificPrices.stream().reduce(Double::sum).get() / instrument0Count;

        var specificStatisticsResponse = getSpecificStatistics(instrument);
        assertThat(specificStatisticsResponse.getBody()).isNotNull();
        assertThat(specificStatisticsResponse.getBody().getCount()).isEqualTo(instrument0Count);
        assertThat(specificStatisticsResponse.getBody().getAvg()).isEqualTo(specificAvg);
        assertThat(specificStatisticsResponse.getBody().getMin())
                .isEqualTo(specificPrices.stream().min(Double::compareTo).get());
        assertThat(specificStatisticsResponse.getBody().getMax())
                .isEqualTo(specificPrices.stream().max(Double::compareTo).get());

    }

    private void fillOtherTicks(ArrayList<Double> prices, int instrument0Count) {
        prices.stream().skip(instrument0Count)
                .forEachOrdered(price->{
                    long timestampInAcceptedRange =
                            timeStrategy.getCurrentTimestampMillis() - RandomUtils.nextLong(10, (acceptedDurationInSeconds - 2) * 1000);
                    postTick(new Tick(randomAlphanumeric(7), price, timestampInAcceptedRange));
                });
    }

    private void fillSpecificInstrumentTicks(String instrument, List<Double> prices) {
        prices.stream().forEachOrdered(price -> {
            long timestampInAcceptedRange =
                    timeStrategy.getCurrentTimestampMillis() - RandomUtils.nextLong(10, (acceptedDurationInSeconds - 2) * 1000);
            postTick(new Tick(instrument, price, timestampInAcceptedRange));
        });
    }


    private ResponseEntity<Void> postTick(Object body) {
        return restTemplate.exchange(TickApi.ROOT_PATH, POST, new HttpEntity<>(body), Void.class);
    }

    private ResponseEntity<PriceStatistics> getSpecificStatistics(String instrument) {
        String url = UriComponentsBuilder.fromPath(PriceStatisticsApi.ROOT_PATH).pathSegment(instrument).toUriString();
        return restTemplate.getForEntity(url, PriceStatistics.class);
    }

    private ResponseEntity<PriceStatistics> getStatistics() {
        String url = PriceStatisticsApi.ROOT_PATH;
        return restTemplate.getForEntity(url, PriceStatistics.class);
    }


}