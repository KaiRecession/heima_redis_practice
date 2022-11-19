package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;


@Component
public class RedisIdWorker {
    // 开始时间戳，由下面的main函数生成
    private static final long BEGIN_TIMESTAMP = 1609459200L;

    private StringRedisTemplate stringRedisTemplate;

    private static final int COUNT_BITS = 32;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }
    public long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        // 只要在32位内就永远不会重复
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        // 防止32位不够，每一天都用新的key
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);
        return timestamp << COUNT_BITS | count;
    }

    public static void main(String[] args) {
        LocalDateTime time = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        long second = time.toEpochSecond(ZoneOffset.UTC);
        // 把这个时间点转换成秒数
        System.out.println(second);
    }
}
