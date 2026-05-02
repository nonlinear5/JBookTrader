package com.jbooktrader.platform.test;

import com.jbooktrader.platform.chart.TimedValue;
import com.jbooktrader.platform.performance.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nonlinear5
 */
public class GeometricUtilityTest {
    private static final double tolerance = 1E-4;
    private static List<TimedValue> tradeReturns;
    private static double optimalLeverage;


    @Test
    public void testClassicLeverage1a() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 1.));
        tradeReturns.add(new TimedValue(2, 1.));
        tradeReturns.add(new TimedValue(3, 1.));
        tradeReturns.add(new TimedValue(4, 1.));
        tradeReturns.add(new TimedValue(5, 1.));
        tradeReturns.add(new TimedValue(6, 1.));
        tradeReturns.add(new TimedValue(7, -1.));
        tradeReturns.add(new TimedValue(8, -1.));
        tradeReturns.add(new TimedValue(9, -1.));
        tradeReturns.add(new TimedValue(10, -1.));
        optimalLeverageCalculatorTest.evaluate("classic 1a");
        Assert.assertEquals(0.10102051882294473, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage1b() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 1.));
        tradeReturns.add(new TimedValue(2, 1.));
        tradeReturns.add(new TimedValue(3, 1.));
        tradeReturns.add(new TimedValue(4, 1.));
        tradeReturns.add(new TimedValue(5, 1.));
        tradeReturns.add(new TimedValue(6, 1.));
        tradeReturns.add(new TimedValue(7, -1.));
        tradeReturns.add(new TimedValue(8, -1.));
        tradeReturns.add(new TimedValue(9, -1.));
        tradeReturns.add(new TimedValue(10, -1.));
        tradeReturns.add(new TimedValue(11, 1.));
        tradeReturns.add(new TimedValue(12, 1.));
        tradeReturns.add(new TimedValue(13, 1.));
        tradeReturns.add(new TimedValue(14, 1.));
        tradeReturns.add(new TimedValue(15, 1.));
        tradeReturns.add(new TimedValue(16, 1.));
        tradeReturns.add(new TimedValue(17, -1.));
        tradeReturns.add(new TimedValue(18, -1.));
        tradeReturns.add(new TimedValue(19, -1.));
        tradeReturns.add(new TimedValue(20, -1.));

        optimalLeverageCalculatorTest.evaluate("classic 1b");
        Assert.assertEquals(0.10102051882294473, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage2() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, 0.01));
        tradeReturns.add(new TimedValue(7, -0.01));
        tradeReturns.add(new TimedValue(8, -0.01));
        tradeReturns.add(new TimedValue(9, -0.01));
        tradeReturns.add(new TimedValue(10, -0.01));
        optimalLeverageCalculatorTest.evaluate("classic 2");
        Assert.assertEquals(10.102051437854673, optimalLeverage, tolerance);
    }

    @Test
    public void netLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, -0.011));
        optimalLeverageCalculatorTest.evaluate("net loss");
        Assert.assertEquals(0, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage3() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.001));
        tradeReturns.add(new TimedValue(2, 0.001));
        tradeReturns.add(new TimedValue(3, 0.001));
        tradeReturns.add(new TimedValue(4, 0.001));
        tradeReturns.add(new TimedValue(5, 0.001));
        tradeReturns.add(new TimedValue(6, 0.001));
        tradeReturns.add(new TimedValue(7, -0.001));
        tradeReturns.add(new TimedValue(8, -0.001));
        tradeReturns.add(new TimedValue(9, -0.001));
        tradeReturns.add(new TimedValue(10, -0.001));
        optimalLeverageCalculatorTest.evaluate("classic 3");
        Assert.assertEquals(101.02051437746738, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage4() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.001));
        tradeReturns.add(new TimedValue(2, 0.001));
        tradeReturns.add(new TimedValue(3, 0.001));
        tradeReturns.add(new TimedValue(4, 0.001));
        tradeReturns.add(new TimedValue(5, 0.001));
        tradeReturns.add(new TimedValue(6, 0.001));
        tradeReturns.add(new TimedValue(7, 0.001));
        tradeReturns.add(new TimedValue(8, -0.001));
        tradeReturns.add(new TimedValue(9, -0.001));
        tradeReturns.add(new TimedValue(10, -0.001));
        optimalLeverageCalculatorTest.evaluate("high leverage");
        Assert.assertEquals(208.71215201177785, optimalLeverage, tolerance);
    }

    @Test
    public void testClassicLeverage5() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.05));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, 0.01));
        tradeReturns.add(new TimedValue(7, 0.01));
        tradeReturns.add(new TimedValue(8, 0.01));
        tradeReturns.add(new TimedValue(9, 0.01));
        tradeReturns.add(new TimedValue(10, -0.05));
        optimalLeverageCalculatorTest.evaluate("fat loss");
        Assert.assertEquals(5.9306437351083705, optimalLeverage, tolerance);
    }

    @Test
    public void testVeryFatLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.05));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, 0.01));
        tradeReturns.add(new TimedValue(7, 0.01));
        tradeReturns.add(new TimedValue(8, 0.01));
        tradeReturns.add(new TimedValue(9, 0.01));
        tradeReturns.add(new TimedValue(10, -0.09));
        optimalLeverageCalculatorTest.evaluate("very fat loss");
        Assert.assertEquals(1.520707791981662, optimalLeverage, tolerance);
    }

    @Test
    public void testNetLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, -0.01));
        tradeReturns.add(new TimedValue(7, -0.01));
        tradeReturns.add(new TimedValue(8, -0.01));
        tradeReturns.add(new TimedValue(9, -0.01));
        tradeReturns.add(new TimedValue(10, -0.01));
        optimalLeverageCalculatorTest.evaluate("net loss");
        Assert.assertEquals(0, optimalLeverage, tolerance);
    }

    @Test
    public void testVolatileUniform() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.1));
        tradeReturns.add(new TimedValue(2, 0.1));
        tradeReturns.add(new TimedValue(3, 0.1));
        tradeReturns.add(new TimedValue(4, 0.1));
        tradeReturns.add(new TimedValue(5, 0.1));
        tradeReturns.add(new TimedValue(6, -0.1));
        tradeReturns.add(new TimedValue(7, -0.1));
        tradeReturns.add(new TimedValue(8, -0.1));
        tradeReturns.add(new TimedValue(9, -0.1));
        tradeReturns.add(new TimedValue(10, 0.1));
        optimalLeverageCalculatorTest.evaluate("uniform");
        Assert.assertEquals(1.0102051875866698, optimalLeverage, tolerance);
    }


    @Test
    public void testHugeLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.1));
        tradeReturns.add(new TimedValue(2, 0.1));
        tradeReturns.add(new TimedValue(3, 0.1));
        tradeReturns.add(new TimedValue(4, 0.1));
        tradeReturns.add(new TimedValue(5, 0.1));
        tradeReturns.add(new TimedValue(6, 0.1));
        tradeReturns.add(new TimedValue(7, 0.1));
        tradeReturns.add(new TimedValue(8, 0.1));
        tradeReturns.add(new TimedValue(9, 0.1));
        tradeReturns.add(new TimedValue(10, -0.75));
        optimalLeverageCalculatorTest.evaluate("huge loss");
        Assert.assertEquals(0.1035663862052163, optimalLeverage, tolerance);
    }


    @Test
    public void testVince() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, 0.01));
        tradeReturns.add(new TimedValue(7, 0.01));
        tradeReturns.add(new TimedValue(8, 0.01));
        tradeReturns.add(new TimedValue(9, 0.01));
        tradeReturns.add(new TimedValue(10, -0.088));
        optimalLeverageCalculatorTest.evaluate("vince");
        Assert.assertEquals(0.11414611165817846, optimalLeverage, tolerance);
    }

    @Test
    public void testClassicLeverage7() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.01));
        tradeReturns.add(new TimedValue(6, 0.01));
        tradeReturns.add(new TimedValue(7, 0.01));
        tradeReturns.add(new TimedValue(8, 0.01));
        tradeReturns.add(new TimedValue(9, 0.01));
        tradeReturns.add(new TimedValue(10, -0.01));
        optimalLeverageCalculatorTest.evaluate("leverage7");
        Assert.assertEquals(49.99999967038353, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage8() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.01));
        tradeReturns.add(new TimedValue(2, 0.01));
        tradeReturns.add(new TimedValue(3, 0.01));
        tradeReturns.add(new TimedValue(4, 0.01));
        tradeReturns.add(new TimedValue(5, 0.02));
        tradeReturns.add(new TimedValue(6, 0.02));
        tradeReturns.add(new TimedValue(7, 0.03));
        tradeReturns.add(new TimedValue(8, -0.01));
        tradeReturns.add(new TimedValue(9, -0.02));
        tradeReturns.add(new TimedValue(10, -0.03));
        optimalLeverageCalculatorTest.evaluate("case study");
        Assert.assertEquals(6.940949743604275, optimalLeverage, tolerance);
    }


    @Test
    public void testClassicLeverage9() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.15));
        tradeReturns.add(new TimedValue(2, 0.15));
        tradeReturns.add(new TimedValue(10, -0.15));
        optimalLeverageCalculatorTest.evaluate("bad Kelly");
        Assert.assertEquals(1.1438192221255914, optimalLeverage, tolerance);
    }

    @Test
    public void testPaper() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

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
        optimalLeverageCalculatorTest.evaluate("paper test");
        Assert.assertEquals(0.20554168011099883, optimalLeverage, tolerance);
    }

    @Test
    public void testLargeLeverage1() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.001));
        tradeReturns.add(new TimedValue(2, 0.001));
        tradeReturns.add(new TimedValue(3, 0.001));
        tradeReturns.add(new TimedValue(4, 0.001));
        tradeReturns.add(new TimedValue(5, 0.001));
        tradeReturns.add(new TimedValue(6, 0.001));
        tradeReturns.add(new TimedValue(7, -0.001));
        tradeReturns.add(new TimedValue(8, -0.001));
        tradeReturns.add(new TimedValue(9, -0.001));
        tradeReturns.add(new TimedValue(10, -0.001));
        optimalLeverageCalculatorTest.evaluate("large test1");
        Assert.assertEquals(101.02051437746738, optimalLeverage, tolerance);
    }


    @Test
    public void testLargeLeverage2() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.002));
        tradeReturns.add(new TimedValue(2, 0.002));
        tradeReturns.add(new TimedValue(3, 0.002));
        tradeReturns.add(new TimedValue(4, 0.002));
        tradeReturns.add(new TimedValue(5, 0.002));
        tradeReturns.add(new TimedValue(6, 0.002));
        tradeReturns.add(new TimedValue(7, 0.002));
        tradeReturns.add(new TimedValue(8, 0.002));
        tradeReturns.add(new TimedValue(9, 0.002));
        tradeReturns.add(new TimedValue(10, -0.001));
        optimalLeverageCalculatorTest.evaluate("large test2");
        Assert.assertEquals(519.434141848416, optimalLeverage, tolerance);
    }


    @Test
    public void testLargeLeverage3() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.004));
        tradeReturns.add(new TimedValue(2, 0.004));
        tradeReturns.add(new TimedValue(3, 0.004));
        tradeReturns.add(new TimedValue(4, 0.004));
        tradeReturns.add(new TimedValue(5, 0.004));
        tradeReturns.add(new TimedValue(6, 0.004));
        tradeReturns.add(new TimedValue(7, 0.004));
        tradeReturns.add(new TimedValue(8, 0.004));
        tradeReturns.add(new TimedValue(9, 0.004));
        tradeReturns.add(new TimedValue(10, -0.0001));
        optimalLeverageCalculatorTest.evaluate("large test3");
        Assert.assertEquals(3047.744242051266, optimalLeverage, tolerance);
    }


    @Test
    public void halfPercentLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.2));
        tradeReturns.add(new TimedValue(2, 0.2));
        tradeReturns.add(new TimedValue(3, 0.2));
        tradeReturns.add(new TimedValue(4, 0.2));
        tradeReturns.add(new TimedValue(5, 0.2));
        tradeReturns.add(new TimedValue(6, 0.2));
        tradeReturns.add(new TimedValue(7, 0.2));
        tradeReturns.add(new TimedValue(8, 0.2));
        tradeReturns.add(new TimedValue(9, 0.2));
        tradeReturns.add(new TimedValue(10, -0.005));
        optimalLeverageCalculatorTest.evaluate("half percent loss");
        Assert.assertEquals(60.95488972825951, optimalLeverage, tolerance);
    }


    @Test
    public void onePercentLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.2));
        tradeReturns.add(new TimedValue(2, 0.2));
        tradeReturns.add(new TimedValue(3, 0.2));
        tradeReturns.add(new TimedValue(4, 0.2));
        tradeReturns.add(new TimedValue(5, 0.2));
        tradeReturns.add(new TimedValue(6, 0.2));
        tradeReturns.add(new TimedValue(7, 0.2));
        tradeReturns.add(new TimedValue(8, 0.2));
        tradeReturns.add(new TimedValue(9, 0.2));
        tradeReturns.add(new TimedValue(10, -0.01));
        optimalLeverageCalculatorTest.evaluate("one percent loss");
        Assert.assertEquals(37.15662123594943, optimalLeverage, tolerance);
    }

    @Test
    public void twoPercentLoss() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.2));
        tradeReturns.add(new TimedValue(2, 0.2));
        tradeReturns.add(new TimedValue(3, 0.2));
        tradeReturns.add(new TimedValue(4, 0.2));
        tradeReturns.add(new TimedValue(5, 0.2));
        tradeReturns.add(new TimedValue(6, 0.2));
        tradeReturns.add(new TimedValue(7, 0.2));
        tradeReturns.add(new TimedValue(8, 0.2));
        tradeReturns.add(new TimedValue(9, 0.2));
        tradeReturns.add(new TimedValue(10, -0.02));
        optimalLeverageCalculatorTest.evaluate("two percent loss");
        Assert.assertEquals(21.775813464530607, optimalLeverage, tolerance);
    }


    @Test
    public void hugeLeverage() {
        tradeReturns = new ArrayList<>();
        GeometricUtilityTest optimalLeverageCalculatorTest = new GeometricUtilityTest();

        tradeReturns.add(new TimedValue(1, 0.1));
        tradeReturns.add(new TimedValue(2, 0.1));
        tradeReturns.add(new TimedValue(3, 0.1));
        tradeReturns.add(new TimedValue(4, 0.1));
        tradeReturns.add(new TimedValue(5, 0.1));
        tradeReturns.add(new TimedValue(6, 0.1));
        tradeReturns.add(new TimedValue(7, 0.1));
        tradeReturns.add(new TimedValue(8, 0.1));
        tradeReturns.add(new TimedValue(9, 0.1));
        tradeReturns.add(new TimedValue(10, -0.00001));
        optimalLeverageCalculatorTest.evaluate("hugeLeverage");
        Assert.assertEquals(2902.9134865800675, optimalLeverage, tolerance);
    }


    private void evaluate(String text) {
        System.out.println(text);
        MaximumSearch search = new MaximumSearch();

        String kernelName = "Uniform";


        KellyEvaluator kellyEvaluator = new KellyEvaluator(tradeReturns, kernelName);
        double kellyLeverage = search.findArgMax(kellyEvaluator);
        System.out.println("kelly, leverage      :   " + kellyLeverage);
        double growth = kellyEvaluator.evaluateLog(kellyLeverage);

        YoudenEvaluator youdenEvaluator = new YoudenEvaluator(tradeReturns, kernelName, growth / kellyLeverage);
        double youdenLeverage = search.findArgMax(youdenEvaluator);
        System.out.println("youden, leverage :   " + youdenLeverage);

        YoudenEvaluator2 youdenEvaluator2 = new YoudenEvaluator2(tradeReturns, kernelName, kellyLeverage, growth);
        double youdenLeverage2 = search.findArgMax(youdenEvaluator2);
        System.out.println("youden2, leverage:   " + youdenLeverage2);
        System.out.println("delta:   " + (youdenLeverage - youdenLeverage2));


        PowerEvaluatorHalfKelly halfKelly = new PowerEvaluatorHalfKelly(tradeReturns, kernelName);
        double halfKellyLeverage = search.findArgMax(halfKelly);

        optimalLeverage = halfKellyLeverage;
        System.out.println();
    }


}
