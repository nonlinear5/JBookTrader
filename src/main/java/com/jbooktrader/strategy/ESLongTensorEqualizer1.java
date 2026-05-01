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
        // centroid, 250 trades minimum, max OG=616
        addParam(PERIOD1, 10, 10000, 640);
        addParam(SCALE, 1, 1500, 1105);
        addParam(ENTRY, 0, 1000, 199);
        addParam(EXIT, 0, 1000, 431);
    }
}