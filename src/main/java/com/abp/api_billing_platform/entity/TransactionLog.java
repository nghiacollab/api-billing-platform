package com.abp.api_billing_platform.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_logs", indexes = { @Index(columnList = "request_time") })
public class TransactionLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "transaction_id", nullable = false)
  private String transactionId;

  @Column(name = "subscription_id", nullable = false)
  private Long subscriptionId;

  @Column(name = "request_time")
  private LocalDateTime requestTime = LocalDateTime.now();

  @Column(name = "snapshot_unit_price", precision = 15, scale = 4, nullable = false)
  private BigDecimal snapshotUnitPrice;

  @Column(name = "quantity", precision = 15, scale = 4)
  private BigDecimal quantity = BigDecimal.valueOf(1.0000);

  @Column(name = "amount", precision = 15, scale = 4, nullable = false)
  private BigDecimal amount;

  @Column(name = "status", length = 20)
  private String status; // 'SUCCESS' hoặc 'FAILED'

  @Column(name = "error_code", length = 50)
  private String errorCode;

  public Long getId() {
    return id;
  }

  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public Long getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(Long subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public LocalDateTime getRequestTime() {
    return requestTime;
  }

  public void setRequestTime(LocalDateTime requestTime) {
    this.requestTime = requestTime;
  }

  public BigDecimal getSnapshotUnitPrice() {
    return snapshotUnitPrice;
  }

  public void setSnapshotUnitPrice(BigDecimal snapshotUnitPrice) {
    this.snapshotUnitPrice = snapshotUnitPrice;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(String errorCode) {
    this.errorCode = errorCode;
  }
}
