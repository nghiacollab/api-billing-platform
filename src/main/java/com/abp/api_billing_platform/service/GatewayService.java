package com.abp.api_billing_platform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import com.abp.api_billing_platform.dto.ApiKeyInfoDto;
import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.enums.Status;
import com.abp.api_billing_platform.repository.ConsumerRepository;
import com.abp.api_billing_platform.repository.ConsumerSubscriptionRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class GatewayService {
  private final ConsumerSubscriptionRepository subscriptionRepository;
  private final ConsumerRepository consumerRepository;
  private final RestTemplate restTemplate;
  private final TransactionTemplate transactionTemplate;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final RedisService redisService;

  @Autowired
  public GatewayService(ConsumerSubscriptionRepository subscriptionRepository,
      ConsumerRepository consumerRepository,
      PlatformTransactionManager transactionManager,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper, RedisService redisService) {
    this(subscriptionRepository, consumerRepository, transactionManager, kafkaTemplate,
        objectMapper, redisService,
        new RestTemplate());
  }

  GatewayService(ConsumerSubscriptionRepository subscriptionRepository,
      ConsumerRepository consumerRepository,
      PlatformTransactionManager transactionManager,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      RedisService redisService,
      RestTemplate restTemplate) {
    this.subscriptionRepository = subscriptionRepository;
    this.consumerRepository = consumerRepository;
    this.restTemplate = restTemplate;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.redisService = redisService;
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  public String executeRouting(String apiKey, String queryString) {
    String transactionId = "TX_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

    ApiKeyInfoDto cachedInfo = this.redisService.getApiKeyInfo(apiKey);
    if (cachedInfo == null) {
      Optional<ConsumerSubscription> subOpt = subscriptionRepository.findByApiKey(apiKey);
      if (subOpt.isEmpty()) {
        throw new RuntimeException("401_UNAUTHORIZED: API Key không hợp lệ");
      }
      ConsumerSubscription sub = subOpt.get();

      cachedInfo = new ApiKeyInfoDto(
          sub.getId(),
          sub.getConsumer().getId(),
          sub.getMerchantApi().getTargetEndpoint(),
          sub.getStatus(),
          sub.getConsumer().getBillingType(),
          sub.getMerchantApi().getUnitPrice());

      this.redisService.saveApiKey(apiKey, cachedInfo, 60);
    }

    if (!cachedInfo.getStatus().equals(Status.ACTIVE)) {
      throw new RuntimeException("403_FORBIDDEN: Gói API chưa kích hoạt hoặc bị khóa");
    }

    BigDecimal unitPrice = cachedInfo.getUnitPrice();
    BigDecimal quantity = BigDecimal.valueOf(1.0000);
    BigDecimal totalAmount = unitPrice.multiply(quantity);
    boolean isPrepaid = cachedInfo.getBillingType().equals(BillingType.PREPAID);

    if (isPrepaid) {
      final Long consumerId = cachedInfo.getConsumerId();

      boolean isSuccess = transactionTemplate.execute(status -> {
        Consumer consumer = consumerRepository.findById(consumerId)
            .orElseThrow(() -> new RuntimeException("404_NOT_FOUND: Không tìm thấy khách hàng"));

        if (consumer.getWalletBalance().compareTo(totalAmount) < 0) {
          return false;
        }
        consumer.setWalletBalance(consumer.getWalletBalance().subtract(totalAmount));
        consumerRepository.save(consumer);
        return true;
      });

      if (!isSuccess) {
        String notEnoughMoneyLog = buildLogJson(transactionId, cachedInfo.getSubscriptionId(), unitPrice, quantity,
            totalAmount, "FAILED", "402_PAYMENT_REQUIRED");
        kafkaTemplate.send("api-tx-logs", notEnoughMoneyLog);
        throw new RuntimeException("402_PAYMENT_REQUIRED: Số dư tài khoản không đủ để thực hiện gọi API");
      }
    }

    String targetUrl = cachedInfo.getTargetEndpoint();
    if (queryString != null && !queryString.isEmpty()) {
      targetUrl = targetUrl + "?" + queryString;
    }

    String merchantResponse;
    try {
      merchantResponse = restTemplate.getForObject(targetUrl, String.class);
      String successLogPayload = buildLogJson(transactionId, cachedInfo.getSubscriptionId(), unitPrice, quantity,
          totalAmount, "SUCCESS",
          null);
      kafkaTemplate.send("api-tx-logs", successLogPayload);
    } catch (Exception e) {
      if (isPrepaid) {
        final Long consumerId = cachedInfo.getConsumerId();
        transactionTemplate.executeWithoutResult(status -> {
          consumerRepository.findById(consumerId).ifPresent(consumer -> {
            consumer.setWalletBalance(consumer.getWalletBalance().add(totalAmount));
            consumerRepository.save(consumer);
          });
        });
        String errorLogPayload = buildLogJson(transactionId, cachedInfo.getSubscriptionId(), unitPrice, quantity,
            totalAmount, "FAILED",
            "504_MERCHANT_TIMEOUT");
        kafkaTemplate.send("api-tx-logs", errorLogPayload);
        throw new RuntimeException("504_GATEWAY_ERROR: Không thể kết nối tới dịch vụ của Merchant");
      }
      merchantResponse = "Không thể kết nối tới dịch vụ của Merchant";
    }
    return merchantResponse;
  }

  private String buildLogJson(String txId, Long subId, BigDecimal price, BigDecimal qty,
      BigDecimal amount,
      String status, String errCode) {
    try {
      Map<String, Object> logMap = new HashMap<>();
      logMap.put("transactionId", txId);
      logMap.put("subscriptionId", subId);
      logMap.put("snapshotUnitPrice", price);
      logMap.put("quantity", qty);
      logMap.put("amount", amount);
      logMap.put("status", status);
      logMap.put("errorCode", errCode);
      logMap.put("requestTime", LocalDateTime.now());

      return objectMapper.writeValueAsString(logMap);
    } catch (Exception e) {
      return String.format("{\"error\": \"Failed to generate log JSON\", \"message\": \"%s\"}", e.getMessage());
    }
  }
}
