package com.solactive.demo.pricemonitor.components;

/**
 * TimeStrategy.
 *
 * @author Andrey Arefyev
 */
public interface TimeStrategy {
    long getCurrentTimestampMillis();
}