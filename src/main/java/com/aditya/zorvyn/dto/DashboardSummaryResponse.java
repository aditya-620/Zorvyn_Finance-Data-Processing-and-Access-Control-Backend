package com.aditya.zorvyn.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Response DTO for the /api/dashboard/summary endpoint.
 * Aggregates key financial metrics.
 */
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> categoryTotals;
    private long totalRecords;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpenses,
                                     BigDecimal netBalance, Map<String, BigDecimal> categoryTotals,
                                     long totalRecords) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.netBalance = netBalance;
        this.categoryTotals = categoryTotals;
        this.totalRecords = totalRecords;
    }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getNetBalance() { return netBalance; }
    public void setNetBalance(BigDecimal netBalance) { this.netBalance = netBalance; }

    public Map<String, BigDecimal> getCategoryTotals() { return categoryTotals; }
    public void setCategoryTotals(Map<String, BigDecimal> categoryTotals) { this.categoryTotals = categoryTotals; }

    public long getTotalRecords() { return totalRecords; }
    public void setTotalRecords(long totalRecords) { this.totalRecords = totalRecords; }
}
