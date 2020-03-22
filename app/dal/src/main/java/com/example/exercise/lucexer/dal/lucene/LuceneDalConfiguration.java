package com.example.exercise.lucexer.dal.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * LuceneDalConfiguration
 *
 * @author Deven
 * @version : LuceneDalConfiguration, v 0.1 2020-03-11 21:42 Deven Exp$
 */
@Configuration
@ComponentScan("com.example.exercise.lucexer.dal.lucene")
public class LuceneDalConfiguration {



    @Bean
    public Analyzer analyzer() {
        return new SmartChineseAnalyzer();
    }


}
