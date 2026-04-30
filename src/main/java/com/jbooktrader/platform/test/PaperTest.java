package com.jbooktrader.platform.test;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.performance.*;
import org.junit.*;

import java.util.*;

/**
 * @author nonlinear5
 */
public class PaperTest {

    @Test
    public void paperTest() {
        List<TimedValue> tradeReturns = new ArrayList<>();

        tradeReturns.add(new TimedValue(1, 0.1));
        tradeReturns.add(new TimedValue(2, 0.2));
        tradeReturns.add(new TimedValue(3, -0.1));
        tradeReturns.add(new TimedValue(4, -0.2));
        tradeReturns.add(new TimedValue(5, 0.3));
        tradeReturns.add(new TimedValue(6, 0.4));
        tradeReturns.add(new TimedValue(7, -0.3));
        tradeReturns.add(new TimedValue(8, -0.2));
        tradeReturns.add(new TimedValue(9, -0.1));
        tradeReturns.add(new TimedValue(10, 0.1));


        PrudenceEvaluator prudenceEvaluator = new PrudenceEvaluator(tradeReturns, "Uniform");
        MaximumSearch search = new MaximumSearch();
        // ekk
        //double prudentLeverage = search.findRootDF3(prudenceEvaluator);
        //System.out.println("prudent leverage: " + prudentLeverage);

    }
}
