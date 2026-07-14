package com.abp.api_billing_platform.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abp.api_billing_platform.service.GatewayService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/v1/gateway")
public class GatewayController {
  private final GatewayService gatewayService;

  public GatewayController(GatewayService gatewayService) {
    this.gatewayService = gatewayService;
  }

  @GetMapping("/service/**")
  public ResponseEntity<String> handleRedirect(@RequestHeader("X-API-Key") String apiKey, HttpServletRequest request) {
    String queryString = request.getQueryString();
    try {
      String responseBody = this.gatewayService.executeRouting(apiKey, queryString);
      return ResponseEntity.ok(responseBody);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("401_UNAUTHORIZED")) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
      } else if (e.getMessage().contains("402_PAYMENT_REQUIRED")) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(e.getMessage());
      } else {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(e.getMessage());
      }
    }
  }

}
