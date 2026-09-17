package com.sentinelops.service;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertCooldownService{

	private final StringRedisTemplate redisTemplate;

	public AlertCooldownService(StringRedisTemplate redisTemplate){

		this.redisTemplate = redisTemplate;

	}

	public boolean isInCooldown(String key){

		return Boolean.TRUE.equals(redisTemplate.hasKey(key));

	}

	public boolean startCoolDown(String key, long seconds){

		boolean created = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(seconds));

		return Boolean.TRUE.equals(created);

	}

}