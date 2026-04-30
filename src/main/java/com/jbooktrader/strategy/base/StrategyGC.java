package com.jbooktrader.strategy.base;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;

/**
 * @author Eugene Kononov
 */
public abstract class StrategyGC extends Strategy {

    // Gold future
    protected StrategyGC(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("GC", "NYMEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("8:32", "15:25", "America/New_York");
        int multiplier = 100;
        Commission commission = CommissionFactory.getNYMEXFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

}
