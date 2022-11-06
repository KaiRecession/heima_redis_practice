package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    @Override
//    public Result sendCode(String phone, HttpSession session) {
//        // 1、校验手机号
//        if (RegexUtils.isPhoneInvalid(phone)) {
//            // 2、不符合返回错误信息，Result就是一个bean对象，最后会被序列化成json字符串
//            return Result.fail("手机号格式打灭");
//        }
//        // 3、符合，生成验证码
//        String code = RandomUtil.randomNumbers(6);
//        //4、将code保存到session中
//        session.setAttribute("code", code);
//        log.debug("发送短信验证码成功，验证码: {}", code);
//        // 返回ok的Result封装对象
//        return Result.ok();
//    }

//    @Override
//    public Result login(LoginFormDTO loginForm, HttpSession session) {
//        // 1、校验手机号
//        String phone = loginForm.getPhone();
//        if (RegexUtils.isPhoneInvalid(phone)) {
//            // 2、手机号格式错误
//            return Result.fail("手机号格式打灭");
//        }
//        // 3、校验验证码
//        Object cacheCode = session.getAttribute("code");
//        String code = loginForm.getCode();
//        if (code == null || !cacheCode.toString().equals(code)) {
//            // 不一致就返回错误
//            return Result.fail("验证码错误");
//        }
//        User user = query().eq("phone", phone).one();
//        if (user == null) {
//            user = createUserWithPhone(phone);
//        }
//        // user已经存储到session中去
//        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
//        return Result.ok();
//    }


    @Override
    public Result sendCode(String phone, HttpSession session) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式打灭");
        }
        String code = RandomUtil.randomNumbers(6);

        // 并且设置存活时间为两分钟
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        log.debug("发送验证码短信成功，验证码: {}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式打灭");
        }
        // 从redis中获取验证码并校验
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.fail("验证码错误辣");
        }
        User user = query().eq("phone", phone).one();

        if (user == null) {
            user = createUserWithPhone(phone);
        }
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 将user对象转变为map，方便存入redis的hash结构
        // redis的工具类直接收全是String类型的，所以需要把long类型转换为String类型
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((filedName, fieldValue) -> fieldValue.toString()));
        String tokenKey = LOGIN_USER_KEY + token;
        // 一次性将所有的字段存到hash里面
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 设置有效期
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return Result.ok(token);
    }



    private User createUserWithPhone(String phone) {
        // 1、创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 保存用户到数据库
        save(user);
        return user;
    }
}
