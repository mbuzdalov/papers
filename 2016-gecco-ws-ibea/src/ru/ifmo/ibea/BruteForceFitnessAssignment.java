package ru.ifmo.ibea;

public class BruteForceFitnessAssignment extends FitnessAssignment {
    public BruteForceFitnessAssignment(int maxPoints, int maxDimension, double kappa) {
        super(maxPoints, maxDimension, kappa);
    }

    @Override
    protected void assignIncomingIndicators(double[][] points, double[] minima, double[] maxima) {
        int n = points.length;
        int d = points[0].length;
        for (int base = 0; base < n; ++base) {
            double maximum = 0;
            double minimum = 0;
            for (int j = 0; j < n; ++j) {
                double value = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < d; ++k) {
                    value = Math.max(value, points[j][k] - points[base][k]);
                }
                maximum = Math.max(maximum, value);
                minimum = Math.min(minimum, value);
            }
            minima[base] = minimum;
            maxima[base] = maximum;
        }
    }

    @Override
    protected void assignNormalizedFitness(double[][] points, double[] fitness, double quotient) {
        int n = points.length;
        int d = points[0].length;
        for (int base = 0; base < n; ++base) {
            double sum = 0;
            for (int j = 0; j < n; ++j) {
                if (j == base) {
                    continue;
                }
                double value = Double.NEGATIVE_INFINITY;
                for (int k = 0; k < d; ++k) {
                    value = Math.max(value, points[j][k] - points[base][k]);
                }
                sum -= Math.exp(-value / quotient);
            }
            fitness[base] = sum;
        }
    }
}
