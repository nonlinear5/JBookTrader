package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the backtest mode using a data file containing
 * historical market snapshots.
 *
 * @author Eugene Kononov
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {

    DivideAndConquerOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) {
        super(optimizerDialog, strategy, params);
    }

    @Override
    public void optimize() {
        List<StrategyParams> topParams = new LinkedList<>();
        StrategyParams startingParams = new StrategyParams(strategyParams);
        topParams.add(startingParams);
        Set<String> uniqueParams = new HashSet<>();

        int maxRange = 0;
        for (StrategyParam param : startingParams.getAll()) {
            maxRange = Math.max(maxRange, param.getMax() - param.getMin());
        }

        int divider = 3;
        LinkedList<StrategyParams> tasks = new LinkedList<>();
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        int maxPartsPerDimension = prefs.getInt(JBTPreferences.DivideAndConquerCoverage);
        int filteredTasksSize;

        do {
            tasks.clear();
            for (StrategyParams params : topParams) {
                for (StrategyParam param : params.getAll()) {
                    int step = Math.max(1, (param.getMax() - param.getMin()) / (maxPartsPerDimension - 1));
                    param.setStep(step);
                }
                tasks.addAll(getTasks(params, uniqueParams));
            }

            filteredTasksSize = tasks.size();
            passNumber++;
            execute(tasks, passNumber);

            if (cancelled.get()) {
                return;
            }

            if (optimizationResults.isEmpty()) {
                throw new RuntimeException("No strategies found within the specified parameter boundaries.");
            }


            topParams.clear();
            int maxIndex = Math.max(1, (int) Math.log(optimizationResults.size()));
            for (int index = 0; index < maxIndex; index++) {
                StrategyParams params = optimizationResults.get(index).getParams();
                for (StrategyParam param : params.getAll()) {
                    String name = param.getName();
                    int value = param.getValue();
                    int displacement = Math.max(1, param.getStep() / divider);
                    StrategyParam originalParam = strategyParams.get(name);
                    // Don't push beyond the user-specified boundaries
                    param.setMin(Math.max(originalParam.getMin(), value - displacement));
                    param.setMax(Math.min(originalParam.getMax(), value + displacement));
                }
                topParams.add(new StrategyParams(params));
            }

        } while (filteredTasksSize > 0 && !cancelled.get());
    }
}
