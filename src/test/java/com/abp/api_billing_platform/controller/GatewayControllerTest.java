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
  void shouldRouteNestedPathAndQueryStringToGatewayService() {
    GatewayService gatewayService = Mockito.mock(GatewayService.class);
    GatewayController controller = new GatewayController(gatewayService);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/gateway/abc123/mock-merchant/weather");
    request.setQueryString("city=Hanoi");

    when(gatewayService.executeRouting(eq("abc123"), eq("/mock-merchant/weather"), eq("city=Hanoi")))
        .thenReturn("ok");

    ResponseEntity<String> response = controller.handleRedirect("abc123", request);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("ok", response.getBody());
    verify(gatewayService).executeRouting("abc123", "/mock-merchant/weather", "city=Hanoi");
  }
}
