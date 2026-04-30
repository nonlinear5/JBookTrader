package com.jbooktrader.strategy;

import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.es.ESLongSigmaEqualizerSimpleBase;


/**
 * @author Eugene Kononov
 */
public class ESLongEqualizerSample extends ESLongSigmaEqualizerSimpleBase {
    public ESLongEqualizerSample(StrategyParams optimizationParams) {
        super(optimizationParams);
    }

    @Override
    public void setParams() {
        // centroid, 250 trades minimum, max OG=616
        addParam(PERIOD1, 3000, 9000, 6568);
        addParam(SCALE, 100, 1300, 770);
        addParam(ENTRY, 50, 500, 282);
        addParam(DELTA, 50, 600, 183);
        addParam(EXIT, 300, 1000, 740);
    }
}