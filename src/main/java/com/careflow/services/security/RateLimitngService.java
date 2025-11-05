package com.careflow.services.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.RateLimiter;

@Service
public class RateLimitngService {
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    private static final double REQUESTS_PER_SECUNDS = 3.0;

    public boolean tryAcquire(String ipAdress) {
        RateLimiter rateLimiter = limiters.computeIfAbsent(ipAdress, ip -> RateLimiter.create(REQUESTS_PER_SECUNDS));
        return rateLimiter.tryAcquire();
    }
}
