package com.example.exercise.lucexer.dal.lucene.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * LuceneDocAnno
 *
 * @author Deven
 * @version : LuceneDocAnno, v 0.1 2020-03-11 00:11 Deven Exp$
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LuceneDocAnno {

    String indexName();
}
