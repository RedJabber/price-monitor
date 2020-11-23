package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.TickDto;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TickApi.
 *
 * @author Andrey Arefyev
 */

@RequestMapping(TickApi.ROOT)
public interface TickApi {
    String ROOT = "/ticks";

    /**
     *
     * @return - 201 - in case of success, 204 - tick is older than 60 seconds.
     */
    //todo add OpenAPI description
    @PostMapping
    ResponseEntity<Void> tick(@RequestBody TickDto tick);

}