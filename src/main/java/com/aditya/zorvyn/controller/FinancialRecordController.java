package com.aditya.zorvyn.controller;

import com.aditya.zorvyn.model.FinancialRecord;
import com.aditya.zorvyn.service.FinancialRecordService;
import com.aditya.zorvyn.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService financialRecordService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialRecord> createRecord(@Valid @RequestBody FinancialRecord record, Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        FinancialRecord created = financialRecordService.createRecord(record, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<List<FinancialRecord>> getRecords(
            Authentication auth,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        String userId = getUserIdFromAuth(auth);
        List<FinancialRecord> records = financialRecordService.filterRecords(userId, type, category, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ANALYST', 'ADMIN')")
    public ResponseEntity<FinancialRecord> getRecord(@PathVariable String id, Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        return financialRecordService.getRecordById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinancialRecord> updateRecord(@PathVariable String id, @Valid @RequestBody FinancialRecord record, Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        FinancialRecord updated = financialRecordService.updateRecord(id, record, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable String id, Authentication auth) {
        String userId = getUserIdFromAuth(auth);
        financialRecordService.deleteRecord(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String getUserIdFromAuth(Authentication auth) {
        return userService.getUserIdByUsername(auth.getName());
    }
}