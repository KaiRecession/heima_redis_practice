package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{

    private static final String key_prefix = "lock:";

    private String name;

    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean tryLock(Long timeoutSec) {
        long threadId = Thread.currentThread().getId();
        // 就凭这一个值看加锁是否成功
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key_prefix + name, threadId + "", timeoutSec, TimeUnit.SECONDS);
        // 防止success为空指针
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        stringRedisTemplate.delete(key_prefix + name);
    }
}
