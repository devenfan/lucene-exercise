package com.example.exercise.lucexer.dal.lucene.domapper;

/**
 * LuceneDomainMapper
 *
 * @author Deven
 * @version : LuceneDomainMapper, v 0.1 2020-03-09 23:49 Deven Exp$
 */
public interface LuceneDomainMapper<T> {

    T doc2domain(org.apache.lucene.document.Document document);

    org.apache.lucene.document.Document domain2Doc(T bean);


}
