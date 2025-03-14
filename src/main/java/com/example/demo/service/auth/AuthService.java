package com.example.demo.service.auth;

import com.example.demo.common.auth.PrincipalDetails;
import com.example.demo.common.constants.Token;
import com.example.demo.common.enums.Role;
import com.example.demo.entity.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;

    private Authentication createAuthentication(String accessToken) {
        Claims claims = jwtService.parseClaims(accessToken);
        String id = claims.get("id").toString(); // jwt에 넣어준 id값 가져와서 영속성이 없는 User로 생성한 UserDetails을 인자로 넣어줌
        String role = claims.get(Token.AUTHORITIES_KEY, String.class);
        String subject = claims.get("sub", String.class);
        String username = claims.get("username", String.class);
        Collection<? extends GrantedAuthority> authorities = Arrays.stream(role.split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        User user = User.builder()
                .id(Long.parseLong(id))
                .email(subject)
                .username(username)
                .role(Role.from(role))
                .build();
        UserDetails principal = new PrincipalDetails(user); // PrincipalDetails(UserDetails)에 User
        return new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
    }

    public void setAuthentication(String accessToken) {
        Authentication authentication = createAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
