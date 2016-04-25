package ru.ifmo.eps.tests;

import ru.ifmo.eps.*;
import ru.ifmo.eps.omq.*;

public class Tests {
    public static void main(String[] args) {
        new BinaryEpsilonTests(new NaiveBinaryEpsilon()).runTests();
        new BinaryEpsilonTests(new OMQBinaryEpsilon(NaiveOMQ.INSTANCE)).runTests();
        new BinaryEpsilonTests(new OMQBinaryEpsilon(TreeOMQ.INSTANCE)).runTests();
        new BinaryEpsilonTests(new OMQ2BinaryEpsilon()).runTests();
    }
}
