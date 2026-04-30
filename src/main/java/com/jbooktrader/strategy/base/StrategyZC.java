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
public abstract class StrategyZC extends Strategy {

    // Corn future
    protected StrategyZC(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ZC", "ECBOT");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:30", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }
}
