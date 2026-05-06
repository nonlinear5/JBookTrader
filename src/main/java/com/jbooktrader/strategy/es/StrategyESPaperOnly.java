package com.jbooktrader.strategy.es;

import com.ib.client.Contract;
import com.jbooktrader.indicator.combo.TensorEqualizer;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.contract.ContractFactory;

/**
 *
 * This strategy is meant for paper-trading only!
 *
 * @author Eugene Kononov
 */
public abstract class StrategyESPaperOnly extends Strategy {
    protected long counter;

    // S&P 500 e-mini future
    protected StrategyESPaperOnly(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "CME");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("0:20", "23:55", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

    @Override
    public void setIndicators() {
    }

    @Override
    public void onBookSnapshot() {
        counter++;
        if (counter % 60 >= 30) {
            goFlat();
        } else  {
            goLong(1);
        }
    }

}
