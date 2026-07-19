package com.abp.api_billing_platform.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.abp.api_billing_platform.enums.BillingType;
import com.abp.api_billing_platform.enums.Status;

public class ApiKeyInfoDto implements Serializable {
  private Long subscriptionId;
  private Long consumerId;
  private String targetEndpoint;
  private Status status;
  private BillingType billingType;
  private BigDecimal unitPrice;

  public ApiKeyInfoDto() {
  }

  public ApiKeyInfoDto(Long subscriptionId, Long consumerId, String targetEndpoint, Status status,
      BillingType billingType,
      BigDecimal unitPrice) {
    this.subscriptionId = subscriptionId;
    this.consumerId = consumerId;
    this.targetEndpoint = targetEndpoint;
    this.status = status;
    this.billingType = billingType;
    this.unitPrice = unitPrice;
  }

  public Long getSubscriptionId() {
    return subscriptionId;
  }

  public void setSubscriptionId(Long subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  public Long getConsumerId() {
    return consumerId;
  }

  public void setConsumerId(Long consumerId) {
    this.consumerId = consumerId;
  }

  public String getTargetEndpoint() {
    return targetEndpoint;
  }

  public void setTargetEndpoint(String targetEndpoint) {
    this.targetEndpoint = targetEndpoint;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public BillingType getBillingType() {
    return billingType;
  }

  public void setBillingType(BillingType billingType) {
    this.billingType = billingType;
  }

  public BigDecimal getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(BigDecimal unitPrice) {
    this.unitPrice = unitPrice;
  }
}
