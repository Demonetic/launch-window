package com.launchwindow.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ViewingScoreCalculatorTest {
    private final ViewingScoreCalculator calculator = new ViewingScoreCalculator();

    @Test
    void perfectConditions_receiveMaximumScore() {
        short result = calculator.calculate(0, 0, BigDecimal.ZERO, 20000);

        assertEquals(100, result);
    }

    @Test
    void moderateConditions_receiveBalancedScore() {
        short result = calculator.calculate(50, 30, new BigDecimal("20"), 10000);

        assertEquals(55, result);
    }

    @Test
    void poorConditions_receiveMinimumScore() {
        short result = calculator.calculate(100, 100, new BigDecimal("50"), 0);

        assertEquals(0, result);
    }
}
