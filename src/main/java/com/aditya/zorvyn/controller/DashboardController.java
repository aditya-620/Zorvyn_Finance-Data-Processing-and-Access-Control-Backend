package com.aditya.zorvyn.controller;

import com.aditya.zorvyn.dto.DashboardSummaryResponse;
import com.aditya.zorvyn.model.FinancialRecord;
import com.aditya.zorvyn.service.DashboardService;
import com.aditya.zorvyn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VIEWER', 'ANALYST', 'ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * Get a full consolidated dashboard summary in a single call.
     * Includes: totalIncome, totalExpenses, netBalance, categoryTotals, totalRecords.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(Authentication auth) {
        String userId = userService.getUserIdByUsername(auth.getName());

        BigDecimal income = dashboardService.getTotalIncome(userId);
        BigDecimal expenses = dashboardService.getTotalExpenses(userId);
        BigDecimal netBalance = dashboardService.getNetBalance(userId);
        Map<String, BigDecimal> categoryTotals = dashboardService.getCategoryTotals(userId);
        long totalRecords = dashboardService.getTotalRecordCount(userId);

        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                income, expenses, netBalance, categoryTotals, totalRecords
        );

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, BigDecimal>> getCategoryTotals(Authentication auth) {
        String userId = userService.getUserIdByUsername(auth.getName());
        return ResponseEntity.ok(dashboardService.getCategoryTotals(userId));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FinancialRecord>> getRecentActivity(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = userService.getUserIdByUsername(auth.getName());
        return ResponseEntity.ok(dashboardService.getRecentActivity(userId, limit));
    }

    @GetMapping("/trends")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyTrends(
            Authentication auth,
            @RequestParam(defaultValue = "6") int months) {
        String userId = userService.getUserIdByUsername(auth.getName());
        return ResponseEntity.ok(dashboardService.getMonthlyTrends(userId, months));
    }
}