package com.example.detectdanger.service;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String key, int capacity, int duration){
        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> createBucket(capacity,duration)

        );

        return bucket.tryConsume(1);
    }
    private Bucket createBucket(int capacity, int durationHours){
        Bandwidth limit =
                Bandwidth.builder()
                        .capacity(capacity)
                        .refillGreedy(
                                capacity,
                                Duration.ofHours(
                                        durationHours
                                )
                        )
                        .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }



}
