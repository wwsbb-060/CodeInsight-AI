package com.codeinsight.service;

import com.codeinsight.dto.LoginRequest;
import com.codeinsight.dto.LoginResponse;
import com.codeinsight.dto.RegisterRequest;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);
}
