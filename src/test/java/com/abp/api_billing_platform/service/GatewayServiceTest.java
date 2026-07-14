package com.abp.api_billing_platform.service;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.client.RestTemplate;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.entity.TransactionLog;
import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.repository.ConsumerRepository;
import com.abp.api_billing_platform.repository.ConsumerSubscriptionRepository;
import com.abp.api_billing_platform.repository.TransactionLogRepository;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

  @Mock
  private ConsumerSubscriptionRepository subscriptionRepository;

  @Mock
  private ConsumerRepository consumerRepository;

  @Mock
  private TransactionLogRepository transactionLogRepository;

  @Mock
  private PlatformTransactionManager transactionManager;

  @Test
  void shouldPersistLogWhenMerchantCallFails() {
    ConsumerSubscription subscription = new ConsumerSubscription();
    Consumer consumer = new Consumer();
    consumer.setBillingType(BillingType.POSTPAID);
    consumer.setWalletBalance(BigDecimal.TEN);
    subscription.setConsumer(consumer);

    MerchantApi merchantApi = new MerchantApi();
    merchantApi.setUnitPrice(BigDecimal.ONE);
    merchantApi.setTargetEndpoint("http://example.test");
    subscription.setMerchantApi(merchantApi);

    when(subscriptionRepository.findByApiKey("bad-key")).thenReturn(Optional.of(subscription));
    TransactionStatus transactionStatus = org.mockito.Mockito.mock(TransactionStatus.class);
    when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);

    GatewayService service = new GatewayService(subscriptionRepository, consumerRepository, transactionLogRepository,
        transactionManager, new RestTemplate());

    assertThrows(RuntimeException.class, () -> service.executeRouting("bad-key", null));

    verify(transactionLogRepository).save(any(TransactionLog.class));
    verify(consumerRepository, never()).save(any());
  }
}
