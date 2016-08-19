package com.bakkenbaeck.toshi.model;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class OfflineBalance {

    private double amount;

    public double addAmount(final double amount) {
        this.amount += amount;
        return this.amount;
    }

    public double getBalance() {
        return this.amount;
    }

    public double addRandomAmount() {
        final Random r = new Random();
        final double High = 0.0275;
        final double Low = 0.00825;
        final double rawAmount = Low + (High - Low) * r.nextDouble();
        final double roundedAmount = round(rawAmount, 5);
        addAmount(roundedAmount);
        return roundedAmount;
    }

    private double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void reset() {
        amount = 0;
    }

    public void subtract(final double amount) {
        this.amount -= amount;
    }
}
