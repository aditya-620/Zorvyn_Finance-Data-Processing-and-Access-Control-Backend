package com.aditya.zorvyn.service;

import com.aditya.zorvyn.model.FinancialRecord;
import com.aditya.zorvyn.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository financialRecordRepository;
    private final MongoTemplate mongoTemplate;

    public BigDecimal getTotalIncome(String userId) {
        return calculateTotal(userId, "INCOME");
    }

    public BigDecimal getTotalExpenses(String userId) {
        return calculateTotal(userId, "EXPENSE");
    }

    public BigDecimal getNetBalance(String userId) {
        BigDecimal income = getTotalIncome(userId);
        BigDecimal expenses = getTotalExpenses(userId);
        return income.subtract(expenses);
    }

    public Map<String, BigDecimal> getCategoryTotals(String userId) {
        List<FinancialRecord> records = financialRecordRepository.findByUserIdAndDeletedFalse(userId);

        return records.stream()
                .collect(Collectors.groupingBy(
                        FinancialRecord::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, FinancialRecord::getAmount, BigDecimal::add)
                ));
    }

    public List<FinancialRecord> getRecentActivity(String userId, int limit) {
        List<FinancialRecord> records = financialRecordRepository.findByUserIdAndDeletedFalse(userId);
        return records.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<String, BigDecimal> getMonthlyTrends(String userId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        List<FinancialRecord> records = financialRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        return records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getDate().getYear() + "-" + String.format("%02d", record.getDate().getMonthValue()),
                        Collectors.reducing(BigDecimal.ZERO, FinancialRecord::getAmount, BigDecimal::add)
                ));
    }

    private BigDecimal calculateTotal(String userId, String type) {
        MatchOperation match = Aggregation.match(Criteria.where("userId").is(userId).and("type").is(type).and("deleted").is(false));
        GroupOperation group = Aggregation.group().sum("amount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "financial_records", Map.class);

        if (results.getMappedResults().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(results.getMappedResults().get(0).get("total").toString());
    }

    public long getTotalRecordCount(String userId) {
        return financialRecordRepository.findByUserIdAndDeletedFalse(userId).size();
    }
}