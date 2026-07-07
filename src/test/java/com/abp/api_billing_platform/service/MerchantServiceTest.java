package com.abp.api_billing_platform.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abp.api_billing_platform.entity.Merchant;
import com.abp.api_billing_platform.repository.MerchantRepository;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {

  @Mock
  private MerchantRepository merchantRepository;

  @InjectMocks
  private MerchantService merchantService;

  @Test
  void createMerchant_shouldSaveAndReturnMerchant() {
    Merchant merchant = new Merchant();
    merchant.setName("Acme Inc");
    merchant.setMerchantCode("MER-001");

    when(merchantRepository.save(any(Merchant.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Merchant result = merchantService.createMerchant(merchant);

    assertNotNull(result);
    assertEquals("Acme Inc", result.getName());
    verify(merchantRepository).save(merchant);
  }

  @Test
  void getMerchantById_shouldThrowWhenMerchantDoesNotExist() {
    when(merchantRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(IllegalArgumentException.class, () -> merchantService.getMerchantById(99L));
  }

  @Test
  void updateMerchant_shouldUpdateExistingMerchant() {
    Merchant existingMerchant = new Merchant();
    existingMerchant.setName("Old Name");
    existingMerchant.setMerchantCode("MER-OLD");

    Merchant updateRequest = new Merchant();
    updateRequest.setName("New Name");
    updateRequest.setMerchantCode("MER-NEW");

    when(merchantRepository.findById(1L)).thenReturn(Optional.of(existingMerchant));
    when(merchantRepository.save(any(Merchant.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Merchant result = merchantService.updateMerchant(1L, updateRequest);

    assertEquals("New Name", result.getName());
    assertEquals("MER-NEW", result.getMerchantCode());
    verify(merchantRepository).save(existingMerchant);
  }

  @Test
  void deleteMerchant_shouldRemoveExistingMerchant() {
    Merchant merchant = new Merchant();
    merchant.setName("Delete Me");
    merchant.setMerchantCode("MER-DEL");

    when(merchantRepository.findById(2L)).thenReturn(Optional.of(merchant));

    merchantService.deleteMerchant(2L);

    verify(merchantRepository).delete(merchant);
  }

  @Test
  void getAllMerchants_shouldReturnAllMerchants() {
    Merchant merchant = new Merchant();
    merchant.setName("A");
    merchant.setMerchantCode("MER-A");

    when(merchantRepository.findAll()).thenReturn(List.of(merchant));

    List<Merchant> result = merchantService.getAllMerchants();

    assertEquals(1, result.size());
    assertEquals("A", result.get(0).getName());
  }
}
