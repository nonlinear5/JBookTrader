package com.jbooktrader.strategy.base;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.contract.ContractFactory;

/**
 *
 * @author Eugene Kononov
 */
public abstract class StrategyMES extends Strategy {
    // S&P 500 micro future
    protected StrategyMES(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("MES", "CME");

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:05", "15:25", "America/New_York");

        int multiplier = 5;// contract multiplier
        Commission commission = CommissionFactory.getMicroFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

}
