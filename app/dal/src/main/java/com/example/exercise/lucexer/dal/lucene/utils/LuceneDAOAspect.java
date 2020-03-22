package com.example.exercise.lucexer.dal.lucene.utils;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.lucene.LuceneDalDynamicSearcher;
import org.apache.lucene.search.IndexSearcher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
    private LuceneDalDynamicSearcher dynamicSearcher;

    @Around("(execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.search*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.query*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.sum*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.calc*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.count*(..))) || (execution(* com.example.exercise.lucexer.dal.lucene.dao.impl..*.group*(..)))")
    public Object aroundQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        IndexSearcher indexSearcher = LuceneIndexSearchUtils.getIndexSearcherFromThreadLocal();
        if(indexSearcher == null) {
            //如果没有indexSearcher，就新建一个放到ThreadLocal
            try {
                indexSearcher = dynamicSearcher.getSearcherManager().acquire();
                LuceneIndexSearchUtils.setIndexSearcherIntoThreadLocal(indexSearcher);
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw throwable;
            } finally {
                LuceneIndexSearchUtils.setIndexSearcherIntoThreadLocal(null);
                long endTime = System.currentTimeMillis();
                logger.warn("{} Execution Time: {}ms", joinPoint.getSignature(), (endTime - startTime));
            }
        } else {
            //如果有indexSearcher，就只统计执行时间
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                throw throwable;
            } finally {
                long endTime = System.currentTimeMillis();
                logger.warn("{} Execution Time: {}ms", joinPoint.getSignature(), (endTime - startTime));
            }
        }
    }

}
