package com.solactive.demo.pricemonitor.components;

import org.springframework.stereotype.Component;

/**
 * SystemTimeStrategy.
 *
 * @author Andrey Arefyev
 */
@Component
public class SystemTimeStrategy implements TimeStrategy {
    @Override
    public long getCurrentTimestampMillis() {
        return System.currentTimeMillis();
    }
}