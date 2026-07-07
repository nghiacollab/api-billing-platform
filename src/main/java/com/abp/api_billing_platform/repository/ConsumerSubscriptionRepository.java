package com.abp.api_billing_platform.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abp.api_billing_platform.entity.ConsumerSubscription;

public interface ConsumerSubscriptionRepository extends JpaRepository<ConsumerSubscription, Long> {
  Optional<ConsumerSubscription> findByApiKey(String apiKey);
}
