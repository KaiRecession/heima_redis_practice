

-- 获取锁中的线程标识 get key
local id = redis.call('get', KEYS[1])
-- 比较线程标示与锁中的标示是否一致
if (id == ARGV[1]) then
    -- 释放锁 del key
    return redis.call('del', KEYS[1])
end
-- 成功就会返回1， 否则就返回0
return 0