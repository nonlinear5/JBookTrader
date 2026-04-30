package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {

    BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) {
        super(optimizerDialog, strategy, params);
    }

    @Override
    public void optimize() {
        LinkedList<StrategyParams> tasks = getTasks(strategyParams, new HashSet<>());
        execute(tasks, 0);
    }
}
