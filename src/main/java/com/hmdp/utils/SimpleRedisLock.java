package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;


import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{

    private static final String key_prefix = "lock:";
    // 给锁的值设置UUID防止误删别人的锁
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";

    private String name;

    private StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("luas/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);

    }

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public boolean tryLock(Long timeoutSec) {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 就凭这一个值看加锁是否成功
        System.out.println(threadId);
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key_prefix + name, threadId, timeoutSec, TimeUnit.SECONDS);
        // 防止success为空指针
        return Boolean.TRUE.equals(success);
    }

//    @Override
//    public void unlock() {
//        // 获取拼接好的字符串
//        String threadId = ID_PREFIX + Thread.currentThread().getId();
//        String id = stringRedisTemplate.opsForValue().get(key_prefix + name);
//        if (threadId.equals(id)) {
//            stringRedisTemplate.delete(key_prefix + name);
//        }
//    }

    public void unlock() {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key_prefix + name), ID_PREFIX + Thread.currentThread().getId());
    }
}
