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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.enums.BillingType;
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

  @Autowired
  public GatewayService(ConsumerSubscriptionRepository subscriptionRepository,
      ConsumerRepository consumerRepository,
      PlatformTransactionManager transactionManager,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper) {
    this(subscriptionRepository, consumerRepository, transactionManager, kafkaTemplate,
        objectMapper,
        new RestTemplate());
  }

  GatewayService(ConsumerSubscriptionRepository subscriptionRepository,
      ConsumerRepository consumerRepository,
      PlatformTransactionManager transactionManager,
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      RestTemplate restTemplate) {
    this.subscriptionRepository = subscriptionRepository;
    this.consumerRepository = consumerRepository;
    this.restTemplate = restTemplate;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  @Transactional
  public String executeRouting(String apiKey, String queryString) {
    String transactionId = "TX_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

    Optional<ConsumerSubscription> subOpt = subscriptionRepository.findByApiKey(apiKey);
    if (subOpt.isEmpty()) {
      throw new RuntimeException("401_UNAUTHORIZED: API Key không hợp lệ");
    }

    ConsumerSubscription subscription = subOpt.get();
    Consumer consumer = subscription.getConsumer();
    MerchantApi merchantApi = subscription.getMerchantApi();

    BigDecimal unitPrice = merchantApi.getUnitPrice();
    BigDecimal quantity = BigDecimal.valueOf(1.0000);
    BigDecimal totalAmount = unitPrice.multiply(quantity);

    if (consumer.getBillingType() == BillingType.PREPAID) {
      if (consumer.getWalletBalance().compareTo(quantity) < 0) {
        String notEnoughMoneyLogPayload = buildLogJson(transactionId, subscription, unitPrice, quantity, totalAmount,
            "FAILED",
            "402_PAYMENT_REQUIRED");
        kafkaTemplate.send("api-tx-logs", notEnoughMoneyLogPayload);
        throw new RuntimeException("402_PAYMENT_REQUIRED: Số dư tài khoản không đủ để thực hiện gọi API");
      }
      consumer.setWalletBalance(consumer.getWalletBalance().subtract(unitPrice));
      consumerRepository.save(consumer);
    }

    String targetUrl = merchantApi.getTargetEndpoint();
    if (queryString != null && !queryString.isEmpty()) {
      targetUrl = targetUrl + "?" + queryString;
    }

    String merchantResponse;
    try {
      merchantResponse = restTemplate.getForObject(targetUrl, String.class);
      String successLogPayload = buildLogJson(transactionId, subscription, unitPrice, quantity, totalAmount, "SUCCESS",
          null);
      kafkaTemplate.send("api-tx-logs", successLogPayload);
    } catch (Exception e) {
      if (consumer.getBillingType() == BillingType.PREPAID) {
        consumer.setWalletBalance(consumer.getWalletBalance().add(totalAmount));
        consumerRepository.save(consumer);
      }
      String errorLogPayload = buildLogJson(transactionId, subscription, unitPrice, quantity, totalAmount, "FAILED",
          "504_MERCHANT_TIMEOUT");
      kafkaTemplate.send("api-tx-logs", errorLogPayload);
      throw new RuntimeException("504_GATEWAY_ERROR: Không thể kết nối tới dịch vụ của Merchant");
    }
    return merchantResponse;
  }

  private String buildLogJson(String txId, ConsumerSubscription sub, BigDecimal price, BigDecimal qty,
      BigDecimal amount,
      String status, String errCode) {
    try {
      Map<String, Object> logMap = new HashMap<>();
      logMap.put("transactionId", txId);
      logMap.put("subscription", sub);
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
