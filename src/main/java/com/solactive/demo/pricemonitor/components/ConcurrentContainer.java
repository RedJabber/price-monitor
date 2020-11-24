package com.solactive.demo.pricemonitor.components;

/**
 * ConcurentOneItemContainer.
 *
 * @author Andrey Arefyev
 */
public interface ConcurrentContainer<T> {
    T get();
    void set(T newValue);

}