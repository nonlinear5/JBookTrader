package com.jbooktrader.platform.performance;

/**
 * @author Eugene Kononov
 */
public class KernelEvaluator {
    private final KernelType kernelType;

    KernelEvaluator(KernelType kernelType) {
        this.kernelType = kernelType;
    }

    public static KernelType getKernelTypeByName(String name) {
        for (KernelType kernelType : KernelType.values()) {
            if (kernelType.name().equals(name)) {
                return kernelType;
            }
        }
        throw new RuntimeException("Unrecognized kernel name: " + name);
    }

    public double getWeight(double distance) {
        switch (kernelType) {
            case Uniform:
                return 1;
            case Epanechnikov:
                return 0.75 * (1 - distance * distance);
            case Quartic:
                return (15. / 16.) * Math.pow(1 - distance * distance, 2);
            case Natural:
                return 1 - distance * distance;
            case Tricube:
                return (70. / 81.) * Math.pow(1 - Math.pow(Math.abs(distance), 3), 3);
            case Sqrt:
                return 1 - Math.sqrt(distance);
            case Sigmoid:
                return 1. / (1 + Math.exp((distance - 0.5) / 0.075));
            case Exponential:
                return Math.exp(-distance / 0.125);
            case Radial:
                return Math.exp(-distance * distance / 0.25);
            default:
                throw new RuntimeException("Unrecognized kernel type: " + kernelType);
        }
    }

    public enum KernelType {
        Uniform,
        Epanechnikov,
        Quartic,
        Sqrt,
        Tricube,
        Natural,
        Sigmoid,
        Exponential,
        Radial
    }
}
