package com.jbooktrader.strategy.base;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;

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
