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
public abstract class StrategyZN extends Strategy {

    // T-note future
    protected StrategyZN(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ZN", "ECBOT");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:05", "15:25", "America/New_York");
        int multiplier = 1000;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

}
