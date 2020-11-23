package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.TickDto;
import com.solactive.demo.pricemonitor.services.TicksService;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * TickController.
 *
 * @author Andrey Arefyev
 */
@RestController
public class TickController implements TickApi {

    private final TicksService ticksService;

    public TickController(TicksService ticksService) {this.ticksService = ticksService;}

    @Override
    public ResponseEntity<Void> tick(@RequestBody TickDto tick) {
        return ticksService.tryTick(tick, LocalDateTime.now())
               ? ResponseEntity.status(HttpStatus.CREATED).build()
               : ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}