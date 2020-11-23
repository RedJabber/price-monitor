package com.solactive.demo.pricemonitor.services;

import com.solactive.demo.pricemonitor.components.TicksContainer;
import com.solactive.demo.pricemonitor.dto.PriceStatistics;
import com.solactive.demo.pricemonitor.dto.Tick;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicksServiceImpl implements TicksService {
    private final TicksContainer ticksContainer;

    public TicksServiceImpl(TicksContainer ticksContainer) {
        this.ticksContainer = ticksContainer;
    }

    @Override
    public PriceStatistics getAllTicksStatistics() {
        return ticksContainer.getCommonStatistics();
    }

    @Override
    public PriceStatistics getAllTicksStatisticsByInstrument(String instrument) {
        return ticksContainer.getAllTicksStatisticsByInstrument(instrument);
    }

    @Override
    public boolean tryTick(Tick tick, LocalDateTime topThreshold) {
        return ticksContainer.put(tick);
    }
}