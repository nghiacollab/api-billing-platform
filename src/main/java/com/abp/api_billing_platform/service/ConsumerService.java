package com.abp.api_billing_platform.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.enums.AuthType;
import com.abp.api_billing_platform.enums.Status;
import com.abp.api_billing_platform.repository.ConsumerRepository;
import com.abp.api_billing_platform.repository.ConsumerSubscriptionRepository;
import com.abp.api_billing_platform.repository.MerchantApiRepository;

@Service
public class ConsumerService {

  private final ConsumerRepository consumerRepository;
  private final MerchantApiRepository merchantApiRepository;
  private final ConsumerSubscriptionRepository consumerSubscriptionRepository;

  public ConsumerService(ConsumerRepository consumerRepository, MerchantApiRepository merchantApiRepository,
      ConsumerSubscriptionRepository consumerSubscriptionRepository) {
    this.consumerRepository = consumerRepository;
    this.merchantApiRepository = merchantApiRepository;
    this.consumerSubscriptionRepository = consumerSubscriptionRepository;
  }

  public Consumer createConsumer(Consumer consumer) {
    validateConsumer(consumer);
    return consumerRepository.save(consumer);
  }

  public Consumer getConsumerById(Long id) {
    return consumerRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Consumer not found with id: " + id));
  }

  public List<Consumer> getAllConsumers() {
    return consumerRepository.findAll();
  }

  public Consumer updateConsumer(Long id, Consumer consumerDetails) {
    Consumer consumer = getConsumerById(id);
    validateConsumer(consumerDetails);

    consumer.setName(consumerDetails.getName());
    consumer.setBillingType(consumerDetails.getBillingType());
    consumer.setWalletBalance(consumerDetails.getWalletBalance());

    return consumerRepository.save(consumer);
  }

  public void deleteConsumer(Long id) {
    Consumer consumer = getConsumerById(id);
    consumerRepository.delete(consumer);
  }

  public ConsumerSubscription subscribeToApi(Long id, Long merchantApiId, AuthType authType) {
    Consumer consumer = this.getConsumerById(id);
    MerchantApi merchantApi = this.merchantApiRepository.findById(merchantApiId)
        .orElseThrow(() -> new RuntimeException("API not found"));
    ConsumerSubscription sub = new ConsumerSubscription();
    sub.setAuthType(authType);
    sub.setConsumer(consumer);
    sub.setMerchantApi(merchantApi);
    sub.setStatus(Status.ACTIVE);

    String generatedKey = UUID.randomUUID().toString().replace("-", "");
    sub.setApiKey(generatedKey);
    return this.consumerSubscriptionRepository.save(sub);
  }

  private void validateConsumer(Consumer consumer) {
    if (consumer == null) {
      throw new IllegalArgumentException("Consumer cannot be null");
    }

    if (consumer.getName() == null || consumer.getName().isBlank()) {
      throw new IllegalArgumentException("Consumer name is required");
    }

    if (consumer.getBillingType() == null) {
      throw new IllegalArgumentException("Billing type is required");
    }

    if (consumer.getWalletBalance() == null) {
      throw new IllegalArgumentException("Wallet balance is required");
    }
  }
}
