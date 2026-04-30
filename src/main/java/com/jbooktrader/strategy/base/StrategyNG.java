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
public abstract class StrategyNG extends Strategy {

    // Natural gas future
    protected StrategyNG(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("NG", "NYMEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:05", "14:25", "America/New_York");
        int multiplier = 10000; // 10000 million British thermal units (mmBtu)
        Commission commission = CommissionFactory.getNYMEXFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }
}
