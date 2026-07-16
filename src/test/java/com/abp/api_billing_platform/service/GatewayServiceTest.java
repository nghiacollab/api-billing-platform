package com.abp.api_billing_platform.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.repository.ConsumerRepository;
import com.abp.api_billing_platform.repository.ConsumerSubscriptionRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

  @Mock
  private ConsumerSubscriptionRepository subscriptionRepository;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private KafkaTemplate<String, String> kafkaTemplate;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private GatewayService service; // Mockito injects all the mocks declared above here

  @Test
  void shouldSendKafkaLogAndNotSaveConsumerWhenMerchantCallFails_Postpaid() throws Exception {
    // 1. Setup Test Data (Postpaid Consumer)
    ConsumerSubscription subscription = new ConsumerSubscription();
    Consumer consumer = new Consumer();
    consumer.setBillingType(BillingType.POSTPAID);
    consumer.setWalletBalance(BigDecimal.TEN);
    subscription.setConsumer(consumer);

    MerchantApi merchantApi = new MerchantApi();
    merchantApi.setUnitPrice(BigDecimal.ONE);
    merchantApi.setTargetEndpoint("http://example.test");
    subscription.setMerchantApi(merchantApi);

    // Mock API subscription look-up
    when(subscriptionRepository.findByApiKey("bad-key")).thenReturn(Optional.of(subscription));

    // Mock HTTP failure from restTemplate
    when(restTemplate.getForObject("http://example.test", String.class))
        .thenThrow(new RuntimeException("Merchant Timeout Connection Exception"));

    // Mock the ObjectMapper serializing the map to log JSON
    String expectedPayload = "{\"status\":\"FAILED\",\"errCode\":\"504_MERCHANT_TIMEOUT\"}";
    when(objectMapper.writeValueAsString(anyMap())).thenReturn(expectedPayload);

    // 2. Act & Assert: Execute and expect the Gateway Exception to be thrown
    assertThrows(RuntimeException.class, () -> service.executeRouting("bad-key", null));

    // 3. Verifications
    // Verify Kafka message was sent with the simulated payload to the correct topic
    verify(kafkaTemplate).send("api-tx-logs", expectedPayload);

    // Since this is a POSTPAID customer, balance should not be affected/saved on
    // failure
    verify(consumerRepository, never()).save(any());
  }

  @Test
  void shouldRefundConsumerWhenMerchantCallFails_Prepaid() throws Exception {
    // 1. Setup Test Data (Prepaid Consumer)
    ConsumerSubscription subscription = new ConsumerSubscription();
    Consumer consumer = new Consumer();
    consumer.setBillingType(BillingType.PREPAID);
    consumer.setWalletBalance(BigDecimal.TEN); // Starts with $10
    subscription.setConsumer(consumer);

    MerchantApi merchantApi = new MerchantApi();
    merchantApi.setUnitPrice(BigDecimal.ONE); // Cost is $1
    merchantApi.setTargetEndpoint("http://example.test");
    subscription.setMerchantApi(merchantApi);

    when(subscriptionRepository.findByApiKey("prepaid-key")).thenReturn(Optional.of(subscription));

    // Simulate API call failure
    when(restTemplate.getForObject("http://example.test", String.class))
        .thenThrow(new RuntimeException("Timeout"));

    String expectedPayload = "{\"status\":\"FAILED\",\"errCode\":\"504_MERCHANT_TIMEOUT\"}";
    when(objectMapper.writeValueAsString(anyMap())).thenReturn(expectedPayload);

    // 2. Act
    assertThrows(RuntimeException.class, () -> service.executeRouting("prepaid-key", null));

    // 3. Verifications
    // For PREPAID: The balance is debited first (to 9.0), then refunded back (to
    // 10.0) upon failure.
    // Verify that consumer save was called to commit the final state.
    verify(consumerRepository).save(consumer);
    verify(kafkaTemplate).send("api-tx-logs", expectedPayload);
  }
}