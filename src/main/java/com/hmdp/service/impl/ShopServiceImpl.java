package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 使用互斥锁的方式
    @Override
    public Result queryById(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        // 命中空值也失败
        if ("".equals(shopJson)) {
            return Result.fail("店铺信息不存在");
        }
        Shop shop = null;
        String lockKey = "lock:shop:" + id;
        try {
            boolean isLock = tryLock(lockKey);
            if (!isLock) {
                Thread.sleep(50);
                return queryById(id);
            }
            // double check
            shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                shop = JSONUtil.toBean(shopJson, Shop.class);
                return Result.ok(shop);
            }

            shop = getById(id);
            Thread.sleep(200);
            

            if (shop == null) {
                // 将空数据也缓存下来，简单解决缓存穿透问题
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("店铺不存在");
            }

            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            unlock(lockKey);
        }

        return Result.ok(shop);

    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    // 使用逻辑过期的方式
    public Result queryById2(Long id) {
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // 为命中
        if (StrUtil.isBlank(shopJson)) {
            return Result.fail("店铺不存在");
        }

        // 命中
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 如果未过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            return  Result.ok(shop);
        }

        // 已过期重建缓存
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    this.saveShop2Redis(id, 20L);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });

        }

        return Result.ok(shop);

    }

    // 设置逻辑过期，真实的key在redis中不会过期永久存在，这样就不会发生缓存击穿问题
    public void saveShop2Redis(Long id, Long expireSeconds) throws InterruptedException {
        Shop shop = getById(id);
        Thread.sleep(200);
        RedisData redisData = new RedisData();
        // 设置这个对象的shop数据和过期时间
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        System.out.println(11111);
        // 如果id为空肯定就不能更新了
        if (id == null) {
            return Result.fail("商铺的id不能为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return Result.ok();
    }

    public boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        // 防止flag为空指针的问题
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
