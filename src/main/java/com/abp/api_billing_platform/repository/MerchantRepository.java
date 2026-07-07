package com.abp.api_billing_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abp.api_billing_platform.entity.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, Long> {
  Optional<Merchant> findByMerchantCode(String merchantCode);

  boolean existsByMerchantCode(String merchantCode);

  boolean existsByMerchantCodeAndIdNot(String merchantCode, Long id);
}
