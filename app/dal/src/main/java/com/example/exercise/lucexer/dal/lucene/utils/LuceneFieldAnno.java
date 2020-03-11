package com.example.exercise.lucexer.dal.lucene.utils;

import org.apache.lucene.document.Field;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * LuceneFieldAnno
 *
 * @author Deven
 * @version : LuceneFieldAnno, v 0.1 2020-03-11 00:11 Deven Exp$
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LuceneFieldAnno {

    /**
     * Lucene字段名
     */
    String fieldName();

    /**
     * Lucene字段类型
     */
    Class<? extends Field> fieldType();

    /**
     * 是否支持预排序，仅数值类型和String类型可以设置为true
     */
    boolean preSort() default false;
}
