package com.abp.api_billing_platform.interfaces;

import java.math.BigDecimal;

public interface ApiUsageProjection {
  Long getConsumerId();

  Long getMerchantApiId();

  BigDecimal getTotalQuantity();

  BigDecimal getTotalAmount();
}
