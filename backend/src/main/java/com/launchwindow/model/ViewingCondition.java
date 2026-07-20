package com.launchwindow.model;

public enum ViewingCondition {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    VERY_POOR;

    public static ViewingCondition fromScore(short score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Viewing score must be between 0 and 100");
        }

        if (score >= 80) {
            return EXCELLENT;
        }

        if (score >= 60) {
            return GOOD;
        }

        if (score >= 40) {
            return FAIR;
        }

        if (score >= 20) {
            return POOR;
        }

        return VERY_POOR;
    }
}