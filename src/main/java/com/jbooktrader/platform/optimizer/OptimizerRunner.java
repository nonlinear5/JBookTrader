package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.format.*;
import com.jbooktrader.platform.util.ui.*;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static com.jbooktrader.platform.optimizer.PerformanceMetric.*;

/**
 * Runs a trading strategy in the backtest mode using a data file containing
 * historical market snapshots.
 *
 * @author Eugene Kononov
 */
public abstract class OptimizerRunner implements Runnable {
    private static final int MAX_SAVED_RESULTS = 100; // max number of results in the optimization results file
    protected final OptimizerDialog optimizerDialog;
    final List<OptimizationResult> optimizationResults;
    final StrategyParams strategyParams;
    final AtomicBoolean cancelled;
    final AtomicLong completedSteps;
    private final int availableProcessors;
    private final Constructor<?> strategyConstructor;
    private final NumberFormat nf2, nf0, gnf0;
    private final String strategyName;
    private final int minTrades;
    private final String inclusionCriteria;
    private final int strategiesPerProcessor;
    protected int passNumber;
    protected ResultComparator resultComparator;
    private long snapshotCount;
    private ExecutorService optimizationExecutor;
    private ComputationalTimeEstimator timeEstimator;
    private List<MarketSnapshot> snapshots;
    private long totalSteps;
    private long totalStrategies;
    private long lastUpdateTime;

    protected OptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) {
        this.optimizerDialog = optimizerDialog;
        strategyName = strategy.getName();
        strategyParams = params;
        optimizationResults = new CopyOnWriteArrayList<>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf0 = NumberFormatterFactory.getNumberFormatter(0);
        gnf0 = NumberFormatterFactory.getNumberFormatter(0, true);
        availableProcessors = Runtime.getRuntime().availableProcessors() + 1;
        completedSteps = new AtomicLong();
        cancelled = new AtomicBoolean();


        Class<?> clazz;
        try {
            clazz = Class.forName(strategy.getClass().getName());
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Could not find class " + strategy.getClass().getName());
        }

        try {
            strategyConstructor = clazz.getConstructor(StrategyParams.class);
        } catch (NoSuchMethodException nsme) {
            throw new RuntimeException("Could not find strategy constructor for " + strategy.getClass().getName());
        }

        PerformanceMetric performanceMetric = optimizerDialog.getPerformanceMetric();
        resultComparator = new ResultComparator(performanceMetric);
        minTrades = optimizerDialog.getMinTrades();
        inclusionCriteria = optimizerDialog.getInclusionCriteria();
        strategiesPerProcessor = PreferencesHolder.getInstance().getInt(JBTPreferences.StrategiesPerProcessor);
    }

    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        List<List<T>> combinations = Collections.singletonList(Collections.emptyList());
        for (List<T> list : lists) {
            List<List<T>> extraColumnCombinations = new ArrayList<>();
            for (List<T> combination : combinations) {
                for (T element : list) {
                    List<T> newCombination = new ArrayList<>(combination);
                    newCombination.add(element);
                    extraColumnCombinations.add(newCombination);
                }
            }
            combinations = extraColumnCombinations;
        }
        return combinations;
    }

    public Strategy getStrategyInstance(StrategyParams params) {
        try {
            return (Strategy) strategyConstructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    protected abstract void optimize();

    protected void setTotalSteps(long totalSteps) {
        this.totalSteps = totalSteps;
    }

    public int getMinTrades() {
        return minTrades;
    }

    public String getInclusionCriteria() {
        return inclusionCriteria;
    }

    public List<MarketSnapshot> getSnapshots() {
        return snapshots;
    }

    void execute(LinkedList<StrategyParams> tasks, int passNumber) {
        int workerLoad = Math.min(strategiesPerProcessor, Math.max(1, (int) Math.ceil(tasks.size() / (double) availableProcessors)));
        List<Callable<List<OptimizationResult>>> workers = new ArrayList<>();

        optimizerDialog.setProgress("Distributing the workload among the worker threads...");
        totalStrategies = tasks.size();
        while (!tasks.isEmpty()) {
            List<StrategyParams> workerTasks = new ArrayList<>();
            while (!tasks.isEmpty() && workerTasks.size() < workerLoad) {
                workerTasks.add(tasks.removeFirst());
            }
            workers.add(new OptimizerWorker(this, workerTasks));
        }


        setTotalSteps(snapshotCount * totalStrategies);
        timeEstimator = new ComputationalTimeEstimator(totalSteps, availableProcessors);
        completedSteps.set(0);
        String msg = "Starting optimization pass";
        if (passNumber != 0) {
            msg += " #" + passNumber;
        }
        optimizerDialog.setProgress(msg);

        try {
            optimizationExecutor = Executors.newFixedThreadPool(availableProcessors);
            List<Future<List<OptimizationResult>>> workerResults = optimizationExecutor.invokeAll(workers);

            for (Future<List<OptimizationResult>> workerResult : workerResults) {
                List<OptimizationResult> workerOptResults = workerResult.get();
                optimizationResults.addAll(workerOptResults);
            }

            optimizationResults.sort(resultComparator);
            optimizerDialog.setResults(optimizationResults);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            optimizationExecutor.shutdown();

        }
    }

    public void cancel() {
        optimizerDialog.setProgress("Stopping optimization...");
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    private void saveToFile() throws IOException {
        if (optimizationResults.isEmpty()) {
            return;
        }

        String fileName = strategyName + "Optimizer";
        OptimizationReport optimizationReport = new OptimizationReport(fileName);

        optimizationReport.reportDescription("Strategy parameters:");
        for (StrategyParam param : strategyParams.getAll()) {
            optimizationReport.reportDescription(param.toString());
        }
        optimizationReport.reportDescription("Minimum trades for strategy inclusion: " + optimizerDialog.getMinTrades());
        optimizationReport.reportDescription("Back data file: " + optimizerDialog.getFileName());

        List<String> otpimizerReportHeaders = new ArrayList<>();
        StrategyParams params = optimizationResults.iterator().next().getParams();
        for (StrategyParam param : params.getAll()) {
            otpimizerReportHeaders.add(param.getName());
        }

        for (PerformanceMetric performanceMetric : PerformanceMetric.values()) {
            otpimizerReportHeaders.add(performanceMetric.getName());
        }
        optimizationReport.reportHeaders(otpimizerReportHeaders);

        int maxIndex = Math.min(MAX_SAVED_RESULTS, optimizationResults.size());
        for (int index = 0; index < maxIndex; index++) {
            OptimizationResult optimizationResult = optimizationResults.get(index);
            params = optimizationResult.getParams();

            List<String> columns = new ArrayList<>();
            for (StrategyParam param : params.getAll()) {
                columns.add(nf0.format(param.getValue()));
            }

            columns.add(nf0.format(optimizationResult.get(Trades)));
            columns.add(nf0.format(optimizationResult.get(Duration)));
            columns.add(nf0.format(optimizationResult.get(MaxSL)));
            columns.add(nf0.format(optimizationResult.get(MaxDD)));
            columns.add(nf2.format(optimizationResult.get(APD)));
            columns.add(nf2.format(optimizationResult.get(OG)));
            columns.add(nf2.format(optimizationResult.get(PI)));
            columns.add(nf0.format(optimizationResult.get(NetProfit)));
            optimizationReport.report(columns);
        }

    }

    private void showProgress(long counter, String text) {
        String remainingTime = (counter >= totalSteps) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, totalSteps, text + ". Estimated remaining time: " + remainingTime);
    }

    public void iterationsCompleted(long iterationsCompleted) {
        completedSteps.getAndAdd(iterationsCompleted);
        long time = System.currentTimeMillis();

        if (time - lastUpdateTime >= 1000) {
            String msg = (passNumber == 0) ? "Pass #1" : "Pass #" + passNumber;
            msg += ": optimizing " + gnf0.format(totalStrategies) + " strategies";
            showProgress(completedSteps.get(), msg);
            lastUpdateTime = time;
        }
    }

    protected LinkedList<StrategyParams> getTasks(StrategyParams params, Set<String> uniqueParams) {
        List<List<Integer>> lists = new ArrayList<>();

        for (StrategyParam param : params.getAll()) {
            List<Integer> list = new ArrayList<>();
            lists.add(list);
            for (int value = param.getMin(); value <= param.getMax(); value += param.getStep()) {
                list.add(value);
            }
        }

        List<List<Integer>> cartesianProduct = cartesianProduct(lists);
        LinkedList<StrategyParams> tasks = new LinkedList<>();

        for (List<Integer> list : cartesianProduct) {
            StrategyParams strategyParams = new StrategyParams(params);

            for (int index = 0; index < strategyParams.size(); index++) {
                strategyParams.get(index).setValue(list.get(index));
            }

            String key = strategyParams.getKey();
            if (!uniqueParams.contains(key)) {
                tasks.add(strategyParams);
                uniqueParams.add(key);
            }
        }

        return tasks;
    }

    public void run() {
        try {
            optimizationResults.clear();
            optimizerDialog.setResults(optimizationResults);
            optimizerDialog.enableProgress();
            BackTestFileReader backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName(), optimizerDialog.getDateFilter());
            optimizerDialog.setProgress("Loading historical data file...");
            snapshots = backTestFileReader.load(optimizerDialog);
            snapshotCount = snapshots.size();
            passNumber = 0;

            optimizerDialog.setProgress("Starting optimization ...");
            long start = System.currentTimeMillis();
            optimize();

            if (!cancelled.get()) {
                optimizerDialog.setProgress("Setting optimization results ...");
                optimizerDialog.setResults(optimizationResults);
                optimizerDialog.setProgress("Saving optimization results ...");
                saveToFile();
                long end = System.currentTimeMillis();
                long totalTimeInSecs = (end - start) / 1000;
                showProgress(totalSteps, "Optimization");
                optimizerDialog.showMessage("Optimization completed successfully in " + totalTimeInSecs + " seconds.");
            }
        } catch (Throwable t) {
            MessageDialog.showException(t);
        } finally {
            optimizerDialog.signalCompleted();
        }
    }
}
