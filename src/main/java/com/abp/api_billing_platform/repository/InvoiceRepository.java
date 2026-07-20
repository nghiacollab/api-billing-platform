package com.abp.api_billing_platform.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abp.api_billing_platform.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Object> {

}
