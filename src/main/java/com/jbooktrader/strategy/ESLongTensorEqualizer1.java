package com.jbooktrader.strategy;

import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.es.ESLongTensorEqualizerBase;


/**
 * @author Eugene Kononov
 */
public class ESLongTensorEqualizer1 extends ESLongTensorEqualizerBase {
    public ESLongTensorEqualizer1(StrategyParams optimizationParams) {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 10, 10000, 3000);
        addParam(SCALE, 1, 1500, 1092);
        addParam(ENTRY, 0, 1000, 227);
        addParam(EXIT, 0, 1000, 539);
    }
}