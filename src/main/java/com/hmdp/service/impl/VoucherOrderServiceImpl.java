package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.SimpleRedisLock;
import com.hmdp.utils.UserHolder;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀尚未开始");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已经结束");
        }
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        // 要让锁的范围大于事务的范围
        SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        // 这个时间应该要和业务的最长执行时间有关
        boolean isLock = lock.tryLock(1200L);
        if (!isLock) {
            return Result.fail("不允许重复下单");
        }
        try {
            // spring事务的生成是spring对当前对象生成了一个代理对象，所以需要拿到事务对象，又是一个需要研究的问题
            IVoucherOrderService proxy = (IVoucherOrderService)AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);

        } finally {
            lock.unlock();
        }

    }

    // 如果直接对方法加锁，当方法执行完，锁释放了，事务只是提交了，不一定执行完毕
    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        // 一人一单的解决
        Long userId = UserHolder.getUser().getId();
        // 对userID上锁

            Integer count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
            if (count > 0) {
                return Result.fail("你tm已经买过来");
            }
            // set stock = stock - 1 where id = 10 and stock = 100(刚开始查到的值)，这句话能原子
            // mysql有锁，有点复杂，以后看看
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId).gt("stock", 0).update();
            if (!success) {
                return Result.fail("库存不足");
            }
            VoucherOrder voucherOrder = new VoucherOrder();
            // 生成秒杀订单表的id
            long orderId = redisIdWorker.nextId("order");
            // 写入id（主键）
            voucherOrder.setId(orderId);
            // 写入用户ID
            voucherOrder.setUserId(userId);
            // 写入优惠券的id
            voucherOrder.setVoucherId(voucherId);
            save(voucherOrder);
            return Result.ok(orderId);


    }
}
