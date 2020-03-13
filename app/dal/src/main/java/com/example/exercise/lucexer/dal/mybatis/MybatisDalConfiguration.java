package com.example.exercise.lucexer.dal.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisDalConfiguration
 *
 * @author Deven
 * @version : MybatisDalConfiguration, v 0.1 2020-03-10 12:19 Deven Exp$
 */

@Configuration
@ComponentScan("com.example.exercise.lucexer.dal.mybatis")
@MapperScan("com.example.exercise.lucexer.dal.mybatis.dao")
@AutoConfigureAfter({ MybatisAutoConfiguration.class })
public class MybatisDalConfiguration {

}
