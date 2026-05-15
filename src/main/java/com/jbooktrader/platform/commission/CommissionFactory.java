package com.jbooktrader.platform.commission;


/**
 * @author Eugene Kononov
 * <p>
 * For commissions and fees, see
 * http://individuals.interactivebrokers.com/en/accounts/fees/commission.php?ib_entity=llc
 */
public class CommissionFactory {

    private static Commission getCommission(double rate, double min) {
        return new Commission(rate, min);
    }

    /**
     * Futures commissions: http://individuals.interactivebrokers.com/en/p.php?f=commission#futures1
     */
    public static Commission getBundledNorthAmericaFutureCommission() {
        return getCommission(2.25, 2.25); // ES, 6E
    }

    public static Commission getMicroFutureCommission() {
        return getCommission(0.62, 0.62); // MES
    }

    /**
     * Futures commissions: http://individuals.interactivebrokers.com/en/p.php?f=commission#futures1
     */
    public static Commission getNYMEXFutureCommission() {
        return getCommission(2.31, 2.31); // CL, GC, NG
    }

}
