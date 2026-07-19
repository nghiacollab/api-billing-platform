package com.abp.api_billing_platform.kafka_consumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.entity.TransactionLog;
import com.abp.api_billing_platform.repository.TransactionLogRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tools.jackson.databind.ObjectMapper;

@Component
public class TxLogKafkaConsumer {
  private final TransactionLogRepository transactionLogRepository;
  private final ObjectMapper objectMapper;

  @PersistenceContext
  private EntityManager entityManager;

  public TxLogKafkaConsumer(TransactionLogRepository transactionLogRepository, ObjectMapper objectMapper) {
    this.transactionLogRepository = transactionLogRepository;
    this.objectMapper = objectMapper;
  }

  public static class TransactionLogDto {
    public String transactionId;
    public Long subscriptionId;
    public java.math.BigDecimal snapshotUnitPrice;
    public java.math.BigDecimal quantity;
    public java.math.BigDecimal amount;
    public String status;
    public String errorCode;
    public String requestTime;
  }

  @KafkaListener(topics = "api-tx-logs", groupId = "gateway-billing-group", containerFactory = "kafkaListenerContainerFactory")
  @Transactional
  public void consumeLogBatch(List<String> messages) {
    List<TransactionLog> logsToInsert = new ArrayList<>();

    for (String message : messages) {
      try {
        TransactionLogDto dto = objectMapper.readValue(message, TransactionLogDto.class);

        TransactionLog logEntity = new TransactionLog();
        logEntity.setTransactionId(dto.transactionId);
        logEntity.setSnapshotUnitPrice(dto.snapshotUnitPrice);
        logEntity.setQuantity(dto.quantity);
        logEntity.setAmount(dto.amount);
        logEntity.setStatus(dto.status);
        logEntity.setErrorCode(dto.errorCode);

        if (dto.requestTime != null) {
          logEntity.setRequestTime(LocalDateTime.parse(dto.requestTime));
        }

        ConsumerSubscription subProxy = entityManager.getReference(ConsumerSubscription.class, dto.subscriptionId);
        logEntity.setSubscription(subProxy);

        logsToInsert.add(logEntity);
      } catch (Exception e) {
        System.err.println("Failed to parse message: " + message + " | Error: " + e.getMessage());
      }
    }

    if (!logsToInsert.isEmpty()) {
      transactionLogRepository.saveAll(logsToInsert);
      System.out.println("Bulk inserted " + logsToInsert.size() + " records.");
    }
  }
}
