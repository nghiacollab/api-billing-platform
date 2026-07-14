package com.abp.api_billing_platform.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-merchant")
public class MockMerchantController {
  @GetMapping("/weather")
  public ResponseEntity<Map<String, Object>> getWeather(@RequestParam String city) {
    return ResponseEntity.ok(Map.of(
        "city", city,
        "temperature", "28°C",
        "condition", "Sunny",
        "provider", "Merchant Mock Server"));
  }

  @GetMapping("/population")
  public ResponseEntity<Map<String, Integer>> getPopulation(@RequestParam String country) {
    return ResponseEntity.ok(Map.of(
        country, 15000000));
  }

  @GetMapping("/subjects")
  public ResponseEntity<Map<String, List<String>>> getSubjects() {
    return ResponseEntity.ok(Map.of(
        "subjects", List.of("Math", "English", "Science")));
  }

  @GetMapping("/students")
  public ResponseEntity<Map<String, List<String>>> getStudents() {
    return ResponseEntity.ok(Map.of(
        "students", List.of("Mary", "John", "Michael")));
  }

}
