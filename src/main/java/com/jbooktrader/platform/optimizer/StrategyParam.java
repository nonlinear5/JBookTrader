package com.jbooktrader.platform.optimizer;

/**
 * @author Eugene Kononov
 */
public class StrategyParam {
    private final String name;
    private int min, max;
    private int value, step;

    StrategyParam(String name, int min, int max, int step, int value) {
        this.name = name;
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = value;
    }

    // copy constructor
    StrategyParam(StrategyParam param) {
        this(param.name, param.min, param.max, param.step, param.value);
    }

    @Override
    public String toString() {
        return "{" + name + ":" + min + "-" + max + "-" + step + "-" + value + "}";
    }

    public String getName() {
        return name;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getMiddle() {
        return (min + max) / 2d;
    }

    public int getRange() {
        return max - min;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
