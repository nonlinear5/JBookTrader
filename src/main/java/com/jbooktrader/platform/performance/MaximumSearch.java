package com.jbooktrader.platform.performance;

/**
 * @author Eugene Kononov
 */
public class MaximumSearch {

    private static final double tolerance = 1E-4;
    private static final double goldenRatio = (Math.sqrt(5) + 1) / 2;

    public double findArgMax(FunctionEvaluator fe) {
        double left = 0;
        double right = fe.getMaxLeverage();

        while (right - left > tolerance) {
            double displacement = (right - left) / goldenRatio;
            double midLeft = right - displacement;
            double midRight = left + displacement;

            double midLeftValue = fe.evaluate(midLeft);
            double midRightValue = fe.evaluate(midRight);
            if (midLeftValue > midRightValue) {
                right = midRight;
            } else {
                left = midLeft;
            }
        }

        double center = (left + right) / 2;
        return fe.evaluateLog(center) <= 0 ? 0 : center;
    }

}
