package com.aditya.zorvyn.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "financial_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecord {

    @Id
    private String id;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Type is required")
    private String type; // "INCOME" or "EXPENSE"

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private String userId; // Reference to the user who created the record

    private boolean deleted = false; // For soft delete
}