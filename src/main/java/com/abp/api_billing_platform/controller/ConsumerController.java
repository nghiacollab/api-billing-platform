package com.abp.api_billing_platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.abp.api_billing_platform.entity.Consumer;
import com.abp.api_billing_platform.entity.ConsumerSubscription;
import com.abp.api_billing_platform.enums.AuthType;
import com.abp.api_billing_platform.service.ConsumerService;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {
  private final ConsumerService consumerService;

  public ConsumerController(ConsumerService consumerService) {
    this.consumerService = consumerService;
  }

  @PostMapping
  public ResponseEntity<Consumer> createConsumer(@RequestBody Consumer consumer) {
    return ResponseEntity.ok(consumerService.createConsumer(consumer));
  }

  @GetMapping
  public ResponseEntity<List<Consumer>> getAll() {
    return ResponseEntity.ok(consumerService.getAllConsumers());
  }

  @PostMapping("/{id}/subscribe")
  public ResponseEntity<String> subscribeApi(
      @PathVariable Long id,
      @RequestParam Long merchantApiId,
      @RequestParam AuthType authType) {
    ConsumerSubscription sub = this.consumerService.subscribeToApi(id, merchantApiId, authType);
    String merchantCode = sub.getMerchantApi().getMerchant().getMerchantCode();
    String mockUri = UriComponentsBuilder.fromUriString("http://localhost:8080/v1/gateway/service/")
        .path("/{merchantCode}{apiPath}")
        .buildAndExpand(merchantCode, sub.getMerchantApi().getApiPath())
        .toUriString();
    return ResponseEntity.ok("Resister successfully!\nYour API Key: " + sub.getApiKey() + "\nURI: " + mockUri);
  }
}
