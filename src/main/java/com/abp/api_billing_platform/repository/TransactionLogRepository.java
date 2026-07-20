package com.abp.api_billing_platform.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abp.api_billing_platform.entity.TransactionLog;
import com.abp.api_billing_platform.interfaces.ApiUsageProjection;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Object> {
  @Query(value = "SELECT sub.consumer_id as consumerId, sub.merchant_api_id as merchantApiId, " +
      "SUM(tl.quantity) as totalQuantity, " +
      "SUM(tl.amount) as totalAmount " +
      "FROM transaction_logs tl " +
      "JOIN consumer_subscriptions sub ON tl.subscription_id = sub.id " +
      "WHERE tl.status = 'SUCCESS' " +
      "AND tl.request_time >= :startTime AND tl.request_time < :endTime " +
      "GROUP BY sub.consumer_id, sub.merchant_api_id", nativeQuery = true)
  List<ApiUsageProjection> aggregateApiUsageForPeriod(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);
}
