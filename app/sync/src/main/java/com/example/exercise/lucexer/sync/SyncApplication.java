package com.example.exercise.lucexer.sync;

import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.example.exercise.lucexer.dal.mybatis.MybatisDalConfiguration;

@Import({ MybatisDalConfiguration.class, SyncConfiguration.class })
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@SpringBootApplication
public class SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncApplication.class, args);
    }

    @Bean
    public StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper() {
        return new StudentTranscriptLuceneDomainMapper();
    }
}

