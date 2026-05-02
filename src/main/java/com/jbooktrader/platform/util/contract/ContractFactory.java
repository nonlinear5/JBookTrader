package com.jbooktrader.platform.util.contract;

import com.ib.client.Contract;

/**
 * Provides convenience methods to create futures contracts
 *
 * @author Eugene Kononov
 */
public class ContractFactory {

    public static Contract makeContract(String symbol, String securityType, String exchange, String currency) {
        Contract contract = new Contract();

        contract.symbol(symbol);
        contract.secType(securityType);
        contract.exchange("CME");
        contract.currency("USD");
        //contract.localSymbol(symbol);
        //contract.lastTradeDateOrContractMonth("202609");
        //contract.
        //contract.

        return contract;
    }

    public static Contract makeFutureContract(String symbol, String exchange, String currency) {
        return makeContract(symbol, "FUT", exchange, currency);
    }

    public static Contract makeFutureContract(String symbol, String exchange) {
        return makeFutureContract(symbol, exchange, null);
    }
}
