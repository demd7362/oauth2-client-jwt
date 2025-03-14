package com.example.demo.common.config;

import com.example.demo.common.auth.PrincipalDetails;
import com.example.demo.entity.User;
import com.example.demo.model.SecretConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.util.Optional;

@Configuration
@EnableJpaRepositories(basePackages = "com.example.demo.repository")
@EnableJpaAuditing
@RequiredArgsConstructor
public class DatabaseConfig {
    @Bean
    @DependsOn("secretConfig")
    public DataSource dataSource(SecretConfig secretConfig) {
        SecretConfig.Database database = secretConfig.getDatabase();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(database.getJdbcUrl());
        hikariConfig.setUsername(database.getUsername());
        hikariConfig.setPassword(database.getPassword());
        hikariConfig.setDriverClassName(database.getDriverClassName());
        return new HikariDataSource(hikariConfig);
    }


    @Bean
    public AuditorAware<User> auditorProvider() {
        return () -> {
//            if(true){
//                return Optional.of(User.builder()
//                        .id(15L)
//                        .build());
//            } 이렇게 넣어도 id 15로 들어감. 영속성이 없어도 됨
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if(authentication == null){
                return Optional.empty();
            }
            Object principal = authentication.getPrincipal();
            if(principal instanceof PrincipalDetails principalDetails) {
                return Optional.of(principalDetails.user());
            }
            return Optional.empty();
        };
    }
}
