package com.jbooktrader.strategy.base;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.contract.ContractFactory;

/**
 * @author Eugene Kononov
 */
public abstract class StrategyZB extends Strategy {

    // Bond future
    protected StrategyZB(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ZB", "ECBOT");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:30", "America/New_York");
        int multiplier = 1000;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }
}
