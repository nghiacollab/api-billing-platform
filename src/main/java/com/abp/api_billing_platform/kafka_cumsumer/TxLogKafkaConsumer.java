package com.abp.api_billing_platform.kafka_cumsumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.abp.api_billing_platform.entity.TransactionLog;
import com.abp.api_billing_platform.repository.TransactionLogRepository;

import tools.jackson.databind.ObjectMapper;

@Component
public class TxLogKafkaConsumer {
  private final TransactionLogRepository transactionLogRepository;
  private final ObjectMapper objectMapper;

  public TxLogKafkaConsumer(TransactionLogRepository transactionLogRepository, ObjectMapper objectMapper) {
    this.transactionLogRepository = transactionLogRepository;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = "api-tx-logs", groupId = "gateway-billing-group")
  public void consumeLogBatch(List<String> messages) {
    List<TransactionLog> logsToInsert = new ArrayList<>();

    for (String message : messages) {
      try {
        TransactionLog log = objectMapper.readValue(message, TransactionLog.class);
        logsToInsert.add(log);
      } catch (Exception e) {
        System.err.println("Failed to parse message: " + message);
      }
    }

    if (!logsToInsert.isEmpty()) {
      transactionLogRepository.saveAll(logsToInsert);
      System.out.println("Bulk inserted " + logsToInsert.size() + " records.");
    }
  }
}
