package com.abp.api_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abp.api_billing_platform.entity.Consumer;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

}
