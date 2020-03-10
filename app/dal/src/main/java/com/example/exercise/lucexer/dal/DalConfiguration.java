package com.example.exercise.lucexer.dal;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * DalConfiguration
 *
 * @author Deven
 * @version : DalConfiguration, v 0.1 2020-03-10 12:19 Deven Exp$
 */
@MapperScan("com.example.exercise.lucexer.dal.mybatis.dao")
@EnableTransactionManagement
public class DalConfiguration {

}
