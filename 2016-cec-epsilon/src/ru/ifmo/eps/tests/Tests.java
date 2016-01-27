package ru.ifmo.eps.tests;

import ru.ifmo.eps.*;

public class Tests {
    public static void main(String[] args) {
        new BinaryEpsilonTests(new NaiveBinaryEpsilon()).runTests();
        new BinaryEpsilonTests(new BinsearchBinaryEpsilon()).runTests();
    }
}
