package ru.ifmo.eps.tests;

import ru.ifmo.eps.*;

public class NaiveBinaryEpsilonTests extends BinaryEpsilonTests {
    @Override
    protected double runEpsilon(double[][] moving, double[][] fixed) {
        return new NaiveBinaryEpsilon().computeBinaryEpsilon(moving, fixed);
    }
}
