package com.launchwindow.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewingConditionTest {
    @ParameterizedTest
    @CsvSource({
            "100, EXCELLENT",
            "80, EXCELLENT",
            "79, GOOD",
            "60, GOOD",
            "59, FAIR",
            "40, FAIR",
            "39, POOR",
            "20, POOR",
            "19, VERY_POOR",
            "0, VERY_POOR"
    })
    void fromScore_classifiesBoundaryValues(short score, ViewingCondition expected) {
        assertEquals(expected, ViewingCondition.fromScore(score));
    }

    @Test
    void fromScore_rejectsScoreBelowZero() {
        assertThrows(IllegalArgumentException.class, () -> ViewingCondition.fromScore((short) -1));
    }

    @Test
    void fromScore_rejectsScoreAboveOneHundred() {
        assertThrows(IllegalArgumentException.class, () -> ViewingCondition.fromScore((short) 101));
    }
}