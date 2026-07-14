package com.codeinsight.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codeinsight.common.BusinessException;
import com.codeinsight.dto.LoginRequest;
import com.codeinsight.dto.LoginResponse;
import com.codeinsight.dto.RegisterRequest;
import com.codeinsight.entity.User;
import com.codeinsight.mapper.UserMapper;
import com.codeinsight.security.JwtUtils;
import com.codeinsight.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        // Token 存入 Redis（用于登出时黑名单）
        redisTemplate.opsForValue().set(
                "token:" + user.getUsername(),
                token,
                24, TimeUnit.HOURS);

        return new LoginResponse(token, user.getUsername());
    }

    @Override
    public void logout(String token) {
        // JWT 本身无状态，通过 Redis 黑名单实现登出
        String username = jwtUtils.getUsernameFromToken(token);
        redisTemplate.delete("token:" + username);
    }
}
