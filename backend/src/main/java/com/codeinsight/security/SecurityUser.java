package com.codeinsight.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 扩展 Spring Security 的 User，携带 userId。
 * Controller 里可以从 Authentication 中取出，不再需要 hack。
 */
@Getter
public class SecurityUser extends User {

    private final Long userId;

    public SecurityUser(Long userId, String username, String password,
                        Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }
}
