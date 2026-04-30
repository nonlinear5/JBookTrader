package com.jbooktrader.strategy.es;

import com.jbooktrader.indicator.combo.SigmaTensorEqualizerSimplified;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.base.StrategyES;

/**
 * @author Eugene Kononov
 */
public abstract class ESLongSigmaEqualizerSimpleBase extends StrategyES {
    // Strategy parameters names
    protected static final String PERIOD1 = "Period1";
    protected static final String SCALE = "Scale";
    protected static final String ENTRY = "Entry";
    protected static final String DELTA = "Delta";
    protected static final String EXIT = "Exit";

    private static final int maxPositionSize = 3;
    private final int scale, entry, minDelta, exit;
    private double lastFairPriceForce;
    private int targetPosition;

    // indicator
    private SigmaTensorEqualizerSimplified sigmaTensorInd;

    public ESLongSigmaEqualizerSimpleBase(StrategyParams optimizationParams) {
        super(optimizationParams);

        scale = getParam(SCALE);
        entry = getParam(ENTRY);
        minDelta = getParam(DELTA);
        exit = getParam(EXIT);
    }

    @Override
    public void setIndicators() {
        sigmaTensorInd = (SigmaTensorEqualizerSimplified) addIndicator(new SigmaTensorEqualizerSimplified(getParam(PERIOD1), scale));
    }

    @Override
    public void onBookSnapshot() {
        double tension = sigmaTensorInd.getTension();
        double sigmaTension = sigmaTensorInd.getSigmaTension();
        double fairPriceForce = sigmaTensorInd.getFairPriceForce();

        if (tension <= exit) {
            goFlat();
            targetPosition = 0;
            lastFairPriceForce = 0;
        } else if (sigmaTension >= entry) {
            if ((fairPriceForce - lastFairPriceForce) >= minDelta && targetPosition < maxPositionSize) {
                targetPosition = targetPosition == 0 ? 1 : 3;
                lastFairPriceForce = fairPriceForce;
                goLong(targetPosition);
            }
        }
    }
}