package com.emailagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class EmailAgentApplication {
    public static void main(String[] args) {
        // Spring 컨텍스트 초기화 전에 JVM 타임존을 KST로 고정
        // @PostConstruct 사용 시 Hibernate 초기화보다 늦게 실행될 수 있어 main()에서 처리
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        SpringApplication.run(EmailAgentApplication.class, args);
    }
}
