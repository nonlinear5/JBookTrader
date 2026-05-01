package com.jbooktrader.strategy.es;

import com.jbooktrader.indicator.combo.TensorEqualizer;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.strategy.base.StrategyES;

/**
 * @author Eugene Kononov
 */
public abstract class ESLongTensorEqualizerBase extends StrategyES {
    // Strategy parameters names
    protected static final String PERIOD1 = "Period1";
    protected static final String SCALE = "Scale";
    protected static final String ENTRY = "Entry";
    protected static final String EXIT = "Exit";
    private final int scale, entry, exit;


    // indicator
    private TensorEqualizer tensorEqualizer;

    public ESLongTensorEqualizerBase(StrategyParams optimizationParams) {
        super(optimizationParams);

        scale = getParam(SCALE);
        entry = getParam(ENTRY);
        exit = getParam(EXIT);
    }

    @Override
    public void setIndicators() {
        tensorEqualizer = (TensorEqualizer) addIndicator(new TensorEqualizer(getParam(PERIOD1), scale));
    }

    @Override
    public void onBookSnapshot() {
        double tension = tensorEqualizer.getTension();
        double sigmaTension = tensorEqualizer.getSigmaTension();


        if (tension <= exit) {
            goFlat();

        } else if (sigmaTension >= entry) {
            goLong(1);
        }
    }
}
