package com.jbooktrader.platform.model;

/**
 * @author Eugene Kononov
 */
public interface ModelListener {
    void modelChanged(Event event, Object value);

    enum Event {
        StrategyUpdate, SystemStatusUpdate, ModeChanged, Error
    }
}
