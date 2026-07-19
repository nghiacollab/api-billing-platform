package com.abp.api_billing_platform.service;

import java.time.Duration;
import java.util.LinkedHashMap;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.abp.api_billing_platform.dto.ApiKeyInfoDto;

import tools.jackson.databind.ObjectMapper;

@Service
public class RedisService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public ApiKeyInfoDto getApiKeyInfo(String apiKey) {
    String key = "api_key:" + apiKey;
    Object cachedData = this.redisTemplate.opsForValue().get(key);
    if (cachedData != null) {
      if (cachedData instanceof LinkedHashMap) {
        return objectMapper.convertValue(cachedData, ApiKeyInfoDto.class);
      }
      return (ApiKeyInfoDto) cachedData;
    }
    return null;
  }

  public void saveApiKey(String apiKey, ApiKeyInfoDto info, long ttlInMinutes) {
    String key = "api_key:" + apiKey;
    redisTemplate.opsForValue().set(key, info, Duration.ofMinutes(ttlInMinutes));
  }
}
