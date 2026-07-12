package com.abp.api_billing_platform.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MockMerchantControllerTest {

  private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MockMerchantController()).build();

  @Test
  void shouldServeMockWeatherEndpointAtGatewayPath() throws Exception {
    mockMvc.perform(get("/gateway/test-api-key/mock-merchant/weather").param("city", "Hanoi"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.city").value("Hanoi"))
        .andExpect(jsonPath("$.provider").value("Merchant Mock Server"));
  }
}
