package com.abp.api_billing_platform.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.repository.ConsumerRepository;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

  @Mock
  private ConsumerRepository consumerRepository;

  @InjectMocks
  private ConsumerService consumerService;

  @Test
  void createConsumer_shouldSaveAndReturnConsumer() {
    Consumer consumer = new Consumer();
    consumer.setName("Alice");
    consumer.setBillingType(BillingType.PREPAID);
    consumer.setWalletBalance(new BigDecimal("100.50"));

    when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Consumer result = consumerService.createConsumer(consumer);

    assertNotNull(result);
    assertEquals("Alice", result.getName());
    verify(consumerRepository).save(consumer);
  }

  @Test
  void getConsumerById_shouldThrowWhenConsumerDoesNotExist() {
    when(consumerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> consumerService.getConsumerById(99L));
  }

  @Test
  void updateConsumer_shouldUpdateExistingConsumer() {
    Consumer existingConsumer = new Consumer();
    existingConsumer.setName("Old");
    existingConsumer.setBillingType(BillingType.POSTPAID);
    existingConsumer.setWalletBalance(new BigDecimal("10.00"));

    Consumer updateRequest = new Consumer();
    updateRequest.setName("New");
    updateRequest.setBillingType(BillingType.PREPAID);
    updateRequest.setWalletBalance(new BigDecimal("20.00"));

    when(consumerRepository.findById(1L)).thenReturn(Optional.of(existingConsumer));
    when(consumerRepository.save(any(Consumer.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Consumer result = consumerService.updateConsumer(1L, updateRequest);

    assertEquals("New", result.getName());
    assertEquals(BillingType.PREPAID, result.getBillingType());
    assertEquals(new BigDecimal("20.00"), result.getWalletBalance());
    verify(consumerRepository).save(existingConsumer);
  }

  @Test
  void deleteConsumer_shouldRemoveExistingConsumer() {
    Consumer consumer = new Consumer();
    consumer.setName("Delete Me");
    consumer.setBillingType(BillingType.PREPAID);
    consumer.setWalletBalance(new BigDecimal("5.00"));

    when(consumerRepository.findById(2L)).thenReturn(Optional.of(consumer));

    consumerService.deleteConsumer(2L);

    verify(consumerRepository).delete(consumer);
  }

  @Test
  void getAllConsumers_shouldReturnAllConsumers() {
    Consumer consumer = new Consumer();
    consumer.setName("A");
    consumer.setBillingType(BillingType.POSTPAID);
    consumer.setWalletBalance(new BigDecimal("1.00"));

    when(consumerRepository.findAll()).thenReturn(List.of(consumer));

    List<Consumer> result = consumerService.getAllConsumers();

    assertEquals(1, result.size());
    assertEquals("A", result.get(0).getName());
  }
}
