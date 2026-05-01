package com.jbooktrader.platform.test;

import org.junit.*;

import java.util.*;

/**
 * @author nonlinear5
 */
public class PerformanceIndexTest {
    private static final double tolerance = 0.01;
    private static List<Double> tradeReturns;
    private static double optimalLeverage, maxIndex;

    @Test
    public void testClassicLeverage1() {
        tradeReturns = new ArrayList<>();
        PerformanceIndexTest optimalLeverageCalculatorTest = new PerformanceIndexTest();

        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        optimalLeverageCalculatorTest.evaluate();
        Assert.assertEquals(0.0673, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage2() {
        tradeReturns = new ArrayList<>();
        PerformanceIndexTest optimalLeverageCalculatorTest = new PerformanceIndexTest();

        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        tradeReturns.add(-1.);
        optimalLeverageCalculatorTest.evaluate();
        Assert.assertEquals(0.0673, optimalLeverage, tolerance);
    }

    @Test
    public void testClassicLeverage3() {
        tradeReturns = new ArrayList<>();
        PerformanceIndexTest optimalLeverageCalculatorTest = new PerformanceIndexTest();

        tradeReturns.add(0.1);
        tradeReturns.add(0.1);
        tradeReturns.add(0.1);
        tradeReturns.add(0.1);
        tradeReturns.add(0.1);
        tradeReturns.add(0.1);
        tradeReturns.add(-0.1);
        tradeReturns.add(-0.1);
        tradeReturns.add(-0.1);
        tradeReturns.add(-0.1);
        optimalLeverageCalculatorTest.evaluate();
        Assert.assertEquals(0.673, optimalLeverage, tolerance);
    }

    @Test
    public void testRanking() {
        tradeReturns = new ArrayList<>();
        PerformanceIndexTest optimalLeverageCalculatorTest = new PerformanceIndexTest();

        tradeReturns.add(0.1);
        tradeReturns.add(-0.1);
        tradeReturns.add(0.5);
        optimalLeverageCalculatorTest.evaluate();
        double maxIndex1 = maxIndex;
        tradeReturns.clear();
        tradeReturns.add(0.1);
        tradeReturns.add(-0.1);
        tradeReturns.add(1.5);
        optimalLeverageCalculatorTest.evaluate();
        double maxIndex2 = maxIndex;
        Assert.assertTrue(maxIndex2 > maxIndex1);
    }

    private void evaluate() {
        maxIndex = optimalLeverage = 0;
        double index, leverage = 0, step = 0.001;
        do {
            double info = 0;
            leverage += step;

            for (double tradeReturn : tradeReturns) {
                info += 1 / (1 + Math.log1p(leverage * tradeReturn));
            }

            info = -Math.log(info / tradeReturns.size());
            index = Math.sqrt(2 * info * tradeReturns.size());
            if (index > maxIndex) {
                maxIndex = index;
                optimalLeverage = leverage;
            }

        } while (index >= maxIndex);


    }


}
