package com.jbooktrader.strategy;

import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.es.ESLongTensorEqualizerBase;


/**
 * @author Eugene Kononov
 */
public class ESLongTensorEqualizer2 extends ESLongTensorEqualizerBase {
    public ESLongTensorEqualizer2(StrategyParams optimizationParams) {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 10, 10000, 445);
        addParam(SCALE, 1, 1500, 1800);
        addParam(ENTRY, 0, 1000, 268);
        addParam(EXIT, 0, 1000, 456);
    }
}