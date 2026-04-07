package com.aditya.zorvyn.service;

import com.aditya.zorvyn.model.FinancialRecord;
import com.aditya.zorvyn.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository financialRecordRepository;

    public FinancialRecord createRecord(FinancialRecord record, String userId) {
        record.setUserId(userId);
        record.setDeleted(false);
        return financialRecordRepository.save(record);
    }

    public List<FinancialRecord> getRecordsByUser(String userId) {
        return financialRecordRepository.findByUserIdAndDeletedFalse(userId);
    }

    public Optional<FinancialRecord> getRecordById(String id, String userId) {
        return financialRecordRepository.findById(id)
                .filter(record -> !record.isDeleted() && record.getUserId().equals(userId));
    }

    public FinancialRecord updateRecord(String id, FinancialRecord updatedRecord, String userId) {
        FinancialRecord existing = financialRecordRepository.findById(id)
                .filter(record -> !record.isDeleted() && record.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Record not found or access denied"));

        existing.setAmount(updatedRecord.getAmount());
        existing.setType(updatedRecord.getType());
        existing.setCategory(updatedRecord.getCategory());
        existing.setDate(updatedRecord.getDate());
        existing.setNotes(updatedRecord.getNotes());

        return financialRecordRepository.save(existing);
    }

    public void deleteRecord(String id, String userId) {
        FinancialRecord record = financialRecordRepository.findById(id)
                .filter(r -> !r.isDeleted() && r.getUserId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Record not found or access denied"));

        record.setDeleted(true);
        financialRecordRepository.save(record);
    }

    public List<FinancialRecord> filterRecords(String userId, String type, String category, LocalDate startDate, LocalDate endDate) {
        if (type != null && category != null) {
            return financialRecordRepository.findByUserIdAndTypeAndDeletedFalse(userId, type)
                    .stream()
                    .filter(r -> r.getCategory().equals(category))
                    .toList();
        } else if (type != null) {
            return financialRecordRepository.findByUserIdAndTypeAndDeletedFalse(userId, type);
        } else if (category != null) {
            return financialRecordRepository.findByUserIdAndCategoryAndDeletedFalse(userId, category);
        } else if (startDate != null && endDate != null) {
            return financialRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else {
            return getRecordsByUser(userId);
        }
    }
}