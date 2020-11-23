package com.solactive.demo.pricemonitor.controllers;

import com.solactive.demo.pricemonitor.dto.Tick;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @PostMapping
    @Operation(tags = "ticks", description = "Register new tick. No unique check.")
    @ApiResponse(responseCode = "201", description = "Added successfully")
    @ApiResponse(responseCode = "204", description = "Tick is outdated and skipped")
    ResponseEntity<Void> tick(@RequestBody Tick tick);

}