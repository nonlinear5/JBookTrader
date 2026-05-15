package com.jbooktrader.platform.util.contract;

import com.ib.client.Contract;

/**
 * Provides convenience methods to create futures contracts
 *
 * @author Eugene Kononov
 */
public class ContractFactory {

    public static Contract makeContract(String symbol, String localSymbol, String securityType, String exchange, String currency) {
        Contract contract = new Contract();

        contract.symbol(symbol);
        contract.secType(securityType);
        contract.exchange(exchange);
        contract.currency(currency);
        contract.localSymbol(localSymbol);


        return contract;
    }

    public static Contract makeFutureContract(String symbol, String exchange) {
        Contract contract = makeContract(symbol, null, "FUT", exchange, "USD");

        return contract;
    }


}
