package com.jbooktrader.platform.strategy;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.indicator.IndicatorManager;
import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.Mode;
import com.jbooktrader.platform.model.ModelListener.Event;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.ordermanager.OrderManagerAssistant;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.schedule.TradingSchedule;

/**
 * Base class for all classes that implement trading strategies.
 *
 * @author Eugene Kononov
 */
public abstract class Strategy implements Comparable<Strategy> {
    private final StrategyParams params;
    private final Dispatcher dispatcher;
    private final String name;
    private final OrderManagerAssistant orderManagerAssistant;
    private MarketBook marketBook;
    private Contract contract;
    private String ticker;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private StrategyReportManager strategyReportManager;
    private IndicatorManager indicatorManager;

    protected Strategy(StrategyParams params) {
        this.params = params;
        if (params != null && params.size() == 0) {
            setParams();
        }

        name = getClass().getSimpleName();
        dispatcher = Dispatcher.getInstance();
        orderManagerAssistant = dispatcher.getOrderManager().getAssistant();
    }

    /**
     * The framework calls this method when a new snapshot of the limit order book is taken.
     */
    public abstract void onBookSnapshot();

    /**
     * The framework calls this method to set strategy parameter ranges and values.
     */
    protected abstract void setParams();

    /**
     * The framework calls this method to instantiate indicators.
     */
    public abstract void setIndicators();

    public void goFlat() {
        positionManager.setTargetPosition(0);
    }

    public void goLong(int targetPosition) {
        positionManager.setTargetPosition(targetPosition);
    }

    public void goShort(int targetPosition) {
        positionManager.setTargetPosition(-targetPosition);
    }

    public void closePosition() {
        goFlat();
        orderManagerAssistant.trade(this);
    }

    public StrategyParams getParams() {
        return params;
    }

    protected int getParam(String name) {
        return params.get(name).getValue();
    }

    protected void addParam(String name, int min, int max, int value) {
        int step = Math.max(1, (max - min) / 5);
        params.add(name, min, max, step, value);
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public StrategyReportManager getStrategyReportManager() {
        return strategyReportManager;
    }

    public IndicatorManager getIndicatorManager() {
        return indicatorManager;
    }

    public void setIndicatorManager(IndicatorManager indicatorManager) {
        this.indicatorManager = indicatorManager;
        indicatorManager.setMarketBook(marketBook);
    }

    public long getTime() {
        return getMarketBook().getSnapshot().getTime();
    }

    public TradingSchedule getTradingSchedule() {
        return tradingSchedule;
    }

    protected Indicator addIndicator(Indicator indicator) {
        return indicatorManager.addIndicator(indicator);
    }

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, Commission commission) {
        this.contract = contract;
        ticker = contract.symbol();
        contract.multiplier(String.valueOf(multiplier));
        this.tradingSchedule = tradingSchedule;
        performanceManager = new PerformanceManager(this, multiplier, commission);
        positionManager = new PositionManager(this);
        strategyReportManager = new StrategyReportManager(this);
        marketBook = dispatcher.getOrderManager().getAssistant().createMarketBook(this);
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
    }

    public Contract getContract() {
        return contract;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public void processInstant(boolean isInSchedule) {
        if (positionManager.getCurrentPosition() != 0) {
            performanceManager.updateMetrics();
        }

        if (!isInSchedule) {
            goFlat();
        } else {
            onBookSnapshot();
        }

        orderManagerAssistant.trade(this);
    }

    public double getPositionValue() {
        double positionValue = 0;
        MarketBook marketBook = getMarketBook();
        if (marketBook != null) {
            MarketSnapshot marketSnapshot = marketBook.getSnapshot();
            if (marketSnapshot != null) {
                double price = marketSnapshot.getPrice();
                double multiplier = Integer.parseInt(getContract().multiplier());
                int position = getPositionManager().getCurrentPosition();
                positionValue = price * multiplier * position;
            }
        }
        return positionValue;
    }

    void process() {
        if (!marketBook.isEmpty()) {
            boolean hasValidIndicators = indicatorManager.updateIndicators();
            MarketSnapshot marketSnapshot = marketBook.getSnapshot();
            long instant = marketSnapshot.getTime();
            boolean isInSchedule = tradingSchedule.contains(instant);
            isInSchedule = isInSchedule && hasValidIndicators && (dispatcher.getMode() != Mode.ForceClose);
            processInstant(isInSchedule);
            dispatcher.fireModelChanged(Event.StrategyUpdate, this);
        }
    }

    @Override
    public String toString() {
        return name + " [" + getTicker() + "-" + contract.secType() + "-" + contract.exchange() + "]";
    }

    public int compareTo(Strategy other) {
        return name.compareTo(other.name);
    }
}
