package com.example.exercise.lucexer.dal;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * DalConfiguration
 *
 * @author Deven
 * @version : DalConfiguration, v 0.1 2020-03-10 12:19 Deven Exp$
 */

@Configuration
@ComponentScan("com.example.exercise.lucexer.dal")
@MapperScan("com.example.exercise.lucexer.dal.mybatis.dao")
@AutoConfigureAfter({ MybatisAutoConfiguration.class})
public class DalConfiguration {

}
