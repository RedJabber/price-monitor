package com.solactive.demo.pricemonitor.services;

import com.solactive.demo.pricemonitor.components.TicksContainer;
import com.solactive.demo.pricemonitor.dto.AggregatedInfo;
import com.solactive.demo.pricemonitor.dto.TickDto;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicksServiceImpl implements TicksService {
    private final TicksContainer ticksContainer;

    public TicksServiceImpl(TicksContainer ticksContainer) {
        this.ticksContainer = ticksContainer;
    }

    @Override
    public AggregatedInfo getAllTicksStatistics() {
        return ticksContainer.getCommonStatistics();
    }

    @Override
    public AggregatedInfo getAllTicksStatisticsByInstrument(String instrument) {
        return ticksContainer.getAllTicksStatisticsByInstrument(instrument);
    }

    @Override
    public boolean tryTick(TickDto tick, LocalDateTime topThreshold) {
        return ticksContainer.put(tick);
    }
}