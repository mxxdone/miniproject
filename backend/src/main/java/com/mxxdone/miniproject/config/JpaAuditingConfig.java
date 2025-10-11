package com.mxxdone.miniproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    //@EnableJpaAuditing을 별도의 설정 파일로 분리
}