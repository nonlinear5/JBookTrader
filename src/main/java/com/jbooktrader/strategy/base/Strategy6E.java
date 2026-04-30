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
public abstract class Strategy6E extends Strategy {

    // Euro-USD future
    protected Strategy6E(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("6E", "CME");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("2:05", "15:55", "America/New_York");
        int multiplier = 125000;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }
}
