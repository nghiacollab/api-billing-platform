package com.abp.api_billing_platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.abp.api_billing_platform.entity.Merchant;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.enums.Status;
import com.abp.api_billing_platform.repository.MerchantApiRepository;
import com.abp.api_billing_platform.repository.MerchantRepository;

@Service
public class MerchantService {

  private final MerchantRepository merchantRepository;
  private final MerchantApiRepository merchantApiRepository;

  public MerchantService(MerchantRepository merchantRepository, MerchantApiRepository merchantApiRepository) {
    this.merchantRepository = merchantRepository;
    this.merchantApiRepository = merchantApiRepository;
  }

  public Merchant createMerchant(Merchant merchant) {
    validateMerchant(merchant);

    if (merchantRepository.existsByMerchantCode(merchant.getMerchantCode())) {
      throw new IllegalArgumentException("Merchant code already exists");
    }

    return merchantRepository.save(merchant);
  }

  public Merchant getMerchantById(Long id) {
    return merchantRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Merchant not found with id: " + id));
  }

  public List<Merchant> getAllMerchants() {
    return merchantRepository.findAll();
  }

  public Merchant updateMerchant(Long id, Merchant merchantDetails) {
    Merchant merchant = getMerchantById(id);
    validateMerchant(merchantDetails);

    if (merchantRepository.existsByMerchantCodeAndIdNot(merchantDetails.getMerchantCode(), id)) {
      throw new IllegalArgumentException("Merchant code already exists");
    }

    merchant.setName(merchantDetails.getName());
    merchant.setMerchantCode(merchantDetails.getMerchantCode());

    return merchantRepository.save(merchant);
  }

  public void deleteMerchant(Long id) {
    Merchant merchant = getMerchantById(id);
    merchantRepository.delete(merchant);
  }

  public MerchantApi registerApi(Long merchantId, MerchantApi merchantApi) {
    Merchant merchant = this.getMerchantById(merchantId);
    merchantApi.setMerchant(merchant);
    merchantApi.setStatus(Status.ACTIVE);
    return merchantApiRepository.save(merchantApi);
  }

  private void validateMerchant(Merchant merchant) {
    if (merchant == null) {
      throw new IllegalArgumentException("Merchant cannot be null");
    }

    if (merchant.getName() == null || merchant.getName().isBlank()) {
      throw new IllegalArgumentException("Merchant name is required");
    }

    if (merchant.getMerchantCode() == null || merchant.getMerchantCode().isBlank()) {
      throw new IllegalArgumentException("Merchant code is required");
    }
  }
}
