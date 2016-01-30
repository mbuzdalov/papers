package ru.ifmo.eps.tests;

import ru.ifmo.eps.*;
import ru.ifmo.eps.orq.*;

public class Tests {
    public static void main(String[] args) {
        new BinaryEpsilonTests(new NaiveBinaryEpsilon()).runTests();
        new BinaryEpsilonTests(new ORQBinaryEpsilon(NaiveORQ.INSTANCE)).runTests();
        new BinaryEpsilonTests(new ORQBinaryEpsilon(TreeORQ.INSTANCE)).runTests();
        new BinaryEpsilonTests(new ORQ2BinaryEpsilon()).runTests();
    }
}
