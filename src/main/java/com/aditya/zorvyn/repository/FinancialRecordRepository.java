package com.aditya.zorvyn.repository;

import com.aditya.zorvyn.model.FinancialRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends MongoRepository<FinancialRecord, String> {

    List<FinancialRecord> findByUserIdAndDeletedFalse(String userId);

    List<FinancialRecord> findByUserIdAndTypeAndDeletedFalse(String userId, String type);

    List<FinancialRecord> findByUserIdAndCategoryAndDeletedFalse(String userId, String category);

    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 }, 'deleted': false }")
    List<FinancialRecord> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);

    @Query("{ 'userId': ?0, 'deleted': false }")
    List<FinancialRecord> findAllActiveByUserId(String userId);
}