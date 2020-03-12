package com.example.exercise.lucexer.dal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Import;

import com.example.exercise.lucexer.dal.lucene.LuceneDalConfiguration;
import com.example.exercise.lucexer.dal.mybatis.MybatisDalConfiguration;

@Import({ MybatisDalConfiguration.class, LuceneDalConfiguration.class })
@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
