package com.example.exercise.lucexer.dal.lucene.utils;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * LuceneDAOAspect
 *
 * @author Deven
 * @version : LuceneDAOAspect, v 0.1 2020-03-13 12:45 Deven Exp$
 */
@Aspect
@Component
public class LuceneDAOAspect {

    private static final Logger logger = LoggerFactory.getLogger(LuceneDAOAspect.class);

    @Resource
    protected Directory indexDirectory;

    @Around("(execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.search*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.query*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.sum*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.calc*(..)))")
    public Object aroundQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try (IndexReader indexReader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            LuceneThreadLocalUtils.setIndexSearcher(indexSearcher);
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            throw throwable;
        } finally {
            LuceneThreadLocalUtils.setIndexSearcher(null);
            long endTime = System.currentTimeMillis();
            logger.warn("{} Execution Time: {}ms", joinPoint.getSignature(), (endTime - startTime));
        }
    }

}
