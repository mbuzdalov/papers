package ru.ifmo.eps.tests;

import ru.ifmo.eps.*;
import ru.ifmo.eps.orq.*;

public class Tests {
    public static void main(String[] args) {
        new BinaryEpsilonTests(new NaiveBinaryEpsilon()).runTests();
        new BinaryEpsilonTests(new BinsearchBinaryEpsilon()).runTests();
        new BinaryEpsilonTests(new ORQBinaryEpsilon(new NaiveORQ())).runTests();
    }
}
