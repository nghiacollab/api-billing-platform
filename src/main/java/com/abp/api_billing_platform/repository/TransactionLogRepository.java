package com.abp.api_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abp.api_billing_platform.entity.TransactionLog;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, Object> {

}
