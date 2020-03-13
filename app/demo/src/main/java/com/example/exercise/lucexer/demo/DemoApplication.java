package com.example.exercise.lucexer.demo;

import com.example.exercise.lucexer.dal.lucene.LuceneDalConfiguration;
import com.example.exercise.lucexer.dal.mybatis.MybatisDalConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScans({ @ComponentScan("com.example.exercise.lucexer"), })
@Import({ MybatisDalConfiguration.class, LuceneDalConfiguration.class })
@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

