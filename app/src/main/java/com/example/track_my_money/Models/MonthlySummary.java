package com.example.track_my_money.Models;

public class MonthlySummary {

    private final double income;
    private final double expense;

    public MonthlySummary(double income, double expense) {
        this.income = income;
        this.expense = expense;
    }

    public double getIncome() {
        return income;
    }

    public double getExpense() {
        return expense;
    }
}
