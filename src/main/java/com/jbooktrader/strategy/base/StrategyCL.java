package com.jbooktrader.strategy.base;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.commission.CommissionFactory;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.contract.ContractFactory;

/**
 * @author marcus
 */
public abstract class StrategyCL extends Strategy {
    // Crude oil future
    protected StrategyCL(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("CL", "NYMEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:05", "14:05", "America/New_York");
        int multiplier = 1000;// contract 1000 barrels
        Commission commission = CommissionFactory.getNYMEXFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }
}
