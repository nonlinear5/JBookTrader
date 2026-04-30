package com.jbooktrader.strategy.base;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.contract.ContractFactory;

/**
 *  Margin requirements: https://www.interactivebrokers.com/en/index.php?f=26662
 *  Initial margin (as of March 9, 2020): $12,740
 * @author Eugene Kononov
 */
public abstract class StrategyESPaper extends Strategy {
    // S&P 500 e-mini future
    protected StrategyESPaper(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "CME");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("0:20", "23:55", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

}
