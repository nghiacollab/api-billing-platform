package com.abp.api_billing_platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoice_items")
public class InvoiceItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "invoice_id")
  private Invoice invoice;

  @ManyToOne
  @JoinColumn(name = "merchant_api_id")
  private MerchantApi merchantApi;

  @Column(name = "total_quantity", precision = 15, scale = 4)
  private BigDecimal totalQuantity;

  @Column(name = "total_amount", precision = 15, scale = 4)
  private BigDecimal totalAmount;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public Long getId() {
    return id;
  }

  public Invoice getInvoice() {
    return invoice;
  }

  public void setInvoice(Invoice invoice) {
    this.invoice = invoice;
  }

  public MerchantApi getMerchantApi() {
    return merchantApi;
  }

  public void setMerchantApi(MerchantApi merchantApi) {
    this.merchantApi = merchantApi;
  }

  public BigDecimal getTotalQuantity() {
    return totalQuantity;
  }

  public void setTotalQuantity(BigDecimal totalQuantity) {
    this.totalQuantity = totalQuantity;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
