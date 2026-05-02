package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.DefaultOHLCDataset;
import org.jfree.data.xy.OHLCDataItem;
import org.jfree.data.xy.OHLCDataset;

import java.util.*;


/**
 * Encapsulates performance chart data.
 *
 * @author Eugene Kononov
 */
public class PerformanceChartData {

    private final List<TimedValue> strategyProfits, portfolioProfits;
    private final List<OHLCDataItem> portfolioPnL;
    private final List<OHLCDataItem> strategyPnL;
    private final BarSize barSize;
    private final List<OHLCDataItem> prices;
    private final Map<String, Bar> indicatorBars;
    private final Map<String, List<OHLCDataItem>> indicators;
    private final String strategyName;
    private Bar priceBar, strategyPnLbar, portfolioPnLbar;

    public PerformanceChartData(BarSize barSize, List<Indicator> indicators, String strategyName) {
        this.barSize = barSize;

        strategyProfits = new ArrayList<>();
        strategyPnL = new ArrayList<>();
        portfolioProfits = new ArrayList<>();
        portfolioPnL = new ArrayList<>();

        prices = new ArrayList<>();
        indicatorBars = new HashMap<>();
        this.indicators = new HashMap<>();
        this.strategyName = strategyName;
        for (Indicator indicator : indicators) {
            this.indicators.put(indicator.getKey(), new ArrayList<>());
        }
    }

    List<OHLCDataItem> getPrices() {
        return prices;
    }

    public boolean isEmpty() {
        return prices.isEmpty();
    }

    public List<TimedValue> getStrategyProfits() {
        return strategyProfits;
    }

    public List<TimedValue> getPortfolioProfits() {
        return portfolioProfits;
    }


    public TimeSeries getTradeProfitSeries() {
        TimeSeries netProfit = new TimeSeries(strategyName);
        double net = 0;
        for (TimedValue profit : portfolioProfits) {
            net += profit.getValue();
            netProfit.addOrUpdate(new Second(new Date(profit.getTime())), net);
        }
        return netProfit;
    }


    public void updateStrategyPnL(TimedValue profitAndLoss) {
        double value = profitAndLoss.getValue();
        long frequency = barSize.getSize();
        long time = profitAndLoss.getTime();
        strategyProfits.add(profitAndLoss);

        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        int adjustmentValue = (time % frequency == 0) ? 0 : 1;
        long barTime = (completedPeriods + adjustmentValue) * frequency;

        if (strategyPnLbar == null) {
            strategyPnLbar = new Bar(barTime, value);
        }

        if (barTime > strategyPnLbar.getTime()) {
            Date date = new Date(strategyPnLbar.getTime());
            OHLCDataItem item = new OHLCDataItem(date, strategyPnLbar.getOpen(), strategyPnLbar.getHigh(), strategyPnLbar.getLow(), strategyPnLbar.getClose(), 0);
            strategyPnL.add(item);
            strategyPnLbar = new Bar(barTime, profitAndLoss.getValue());
        }

        strategyPnLbar.setClose(value);
        strategyPnLbar.setLow(Math.min(value, strategyPnLbar.getLow()));
        strategyPnLbar.setHigh(Math.max(value, strategyPnLbar.getHigh()));
    }

    public void updatePortfolioPnL(TimedValue profitAndLoss) {
        double value = profitAndLoss.getValue();
        long frequency = barSize.getSize();
        long time = profitAndLoss.getTime();
        portfolioProfits.add(profitAndLoss);

        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        int adjustmentValue = (time % frequency == 0) ? 0 : 1;
        long barTime = (completedPeriods + adjustmentValue) * frequency;

        if (portfolioPnLbar == null) {
            portfolioPnLbar = new Bar(barTime, value);
        }

        if (barTime > portfolioPnLbar.getTime()) {
            Date date = new Date(portfolioPnLbar.getTime());
            OHLCDataItem item = new OHLCDataItem(date, portfolioPnLbar.getOpen(), portfolioPnLbar.getHigh(), portfolioPnLbar.getLow(), portfolioPnLbar.getClose(), 0);
            portfolioPnL.add(item);
            portfolioPnLbar = new Bar(barTime, profitAndLoss.getValue());
        }

        portfolioPnLbar.setClose(value);
        portfolioPnLbar.setLow(Math.min(value, portfolioPnLbar.getLow()));
        portfolioPnLbar.setHigh(Math.max(value, portfolioPnLbar.getHigh()));
    }

    public void update(List<Indicator> indicatorsToUpdate, long time) {
        long frequency = barSize.getSize();
        // integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        int adjustmentValue = (time % frequency == 0) ? 0 : 1;
        long barTime = (completedPeriods + adjustmentValue) * frequency;

        for (Indicator indicator : indicatorsToUpdate) {
            double value = indicator.getValue();

            Bar indicatorBar = indicatorBars.computeIfAbsent(indicator.getKey(), k -> new Bar(barTime, value));

            if (barTime > indicatorBar.getTime()) {
                Date date = new Date(indicatorBar.getTime());
                OHLCDataItem item = new OHLCDataItem(date, indicatorBar.getOpen(), indicatorBar.getHigh(), indicatorBar.getLow(), indicatorBar.getClose(), 0);
                List<OHLCDataItem> ind = indicators.get(indicator.getKey());
                ind.add(item);
                indicatorBar = new Bar(barTime, value);
                indicatorBars.put(indicator.getKey(), indicatorBar);
            }

            indicatorBar.setClose(value);
            indicatorBar.setLow(Math.min(value, indicatorBar.getLow()));
            indicatorBar.setHigh(Math.max(value, indicatorBar.getHigh()));
        }
    }

    public void update(MarketSnapshot marketSnapshot) {
        long frequency = barSize.getSize();
        long time = marketSnapshot.getTime();
        double price = marketSnapshot.getPrice();

        // Integer division gives us the number of whole periods
        long completedPeriods = time / frequency;
        int adjustmentValue = (time % frequency == 0) ? 0 : 1;
        long barTime = (completedPeriods + adjustmentValue) * frequency;

        if (priceBar == null) {
            priceBar = new Bar(barTime, price);
        }

        if (barTime > priceBar.getTime()) {
            Date date = new Date(priceBar.getTime());
            OHLCDataItem item = new OHLCDataItem(date, priceBar.getOpen(), priceBar.getHigh(), priceBar.getLow(), priceBar.getClose(), 0);
            prices.add(item);
            priceBar = new Bar(barTime, price);
        }

        priceBar.setClose(price);
        priceBar.setLow(Math.min(price, priceBar.getLow()));
        priceBar.setHigh(Math.max(price, priceBar.getHigh()));
    }

    OHLCDataset getPriceDataset() {
        return new DefaultOHLCDataset("", prices.toArray(new OHLCDataItem[0]));
    }


    OHLCDataset getStrategyNetProfitDataset() {
        return new DefaultOHLCDataset("", strategyPnL.toArray(new OHLCDataItem[0]));
    }

    OHLCDataset getPortfolioNetProfitDataset() {
        return new DefaultOHLCDataset("", portfolioPnL.toArray(new OHLCDataItem[0]));
    }

    OHLCDataset getIndicatorDataset(Indicator indicator) {
        List<OHLCDataItem> ind = indicators.get(indicator.getKey());
        return new DefaultOHLCDataset("", ind.toArray(new OHLCDataItem[0]));
    }

}
