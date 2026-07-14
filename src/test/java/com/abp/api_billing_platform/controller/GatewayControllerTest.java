package com.abp.api_billing_platform.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import com.abp.api_billing_platform.service.GatewayService;

class GatewayControllerTest {

  @Test
  void shouldRouteWithHeaderApiKeyAndQueryString() {
    GatewayService gatewayService = Mockito.mock(GatewayService.class);
    GatewayController controller = new GatewayController(gatewayService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/gateway/service/mock-merchant/weather");
    request.setQueryString("city=Hanoi");
    request.addHeader("X-API-Key", "test-api-key-123");

    when(gatewayService.executeRouting(eq("test-api-key-123"), eq("city=Hanoi")))
        .thenReturn("{\"city\":\"Hanoi\",\"temperature\":\"28°C\"}");

    ResponseEntity<String> response = controller.handleRedirect("test-api-key-123", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("{\"city\":\"Hanoi\",\"temperature\":\"28°C\"}", response.getBody());
    verify(gatewayService).executeRouting("test-api-key-123", "city=Hanoi");
  }

  @Test
  void shouldReturn401WhenApiKeyIsInvalid() {
    GatewayService gatewayService = Mockito.mock(GatewayService.class);
    GatewayController controller = new GatewayController(gatewayService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/gateway/service/mock-merchant/weather");
    request.addHeader("X-API-Key", "invalid-key");

    when(gatewayService.executeRouting(eq("invalid-key"), eq(null)))
        .thenThrow(new RuntimeException("401_UNAUTHORIZED: API Key không hợp lệ"));

    ResponseEntity<String> response = controller.handleRedirect("invalid-key", request);

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("401_UNAUTHORIZED: API Key không hợp lệ", response.getBody());
  }

  @Test
  void shouldReturn402WhenInsufficientBalance() {
    GatewayService gatewayService = Mockito.mock(GatewayService.class);
    GatewayController controller = new GatewayController(gatewayService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/v1/gateway/service/mock-merchant/weather");
    request.addHeader("X-API-Key", "test-key");

    when(gatewayService.executeRouting(eq("test-key"), eq(null)))
        .thenThrow(new RuntimeException("402_PAYMENT_REQUIRED: Số dư tài khoản không đủ"));

    ResponseEntity<String> response = controller.handleRedirect("test-key", request);

    assertEquals(HttpStatus.PAYMENT_REQUIRED, response.getStatusCode());
  }
}
