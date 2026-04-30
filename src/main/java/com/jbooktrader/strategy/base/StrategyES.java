package com.jbooktrader.strategy.base;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.contract.*;

/**
 *  Margin requirements: https://www.interactivebrokers.com/en/index.php?f=26662
 *  Initial margin (as of March 9, 2020): $12,740
 * @author Eugene Kononov
 */
public abstract class StrategyES extends Strategy {
    // S&P 500 e-mini future
    protected StrategyES(StrategyParams optimizationParams) {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "CME");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:05", "15:25", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);
    }

}
