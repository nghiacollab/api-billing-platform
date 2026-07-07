package com.abp.api_billing_platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abp.api_billing_platform.entity.Merchant;
import com.abp.api_billing_platform.entity.MerchantApi;
import com.abp.api_billing_platform.service.MerchantService;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
  private final MerchantService merchantService;

  public MerchantController(MerchantService merchantService) {
    this.merchantService = merchantService;
  }

  @PostMapping
  public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
    return ResponseEntity.ok(merchantService.createMerchant(merchant));
  }

  @GetMapping
  public ResponseEntity<List<Merchant>> getAll() {
    return ResponseEntity.ok(merchantService.getAllMerchants());
  }

  @PostMapping("/{id}/apis")
  public ResponseEntity<MerchantApi> registerApi(@PathVariable Long id, @RequestBody MerchantApi api) {
    return ResponseEntity.ok(merchantService.registerApi(id, api));
  }
}
