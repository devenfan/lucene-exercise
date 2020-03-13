package com.example.exercise.lucexer.dal.lucene.utils;

import org.apache.lucene.search.IndexSearcher;

/**
 * ThreadLocalUtils
 *
 * @author Deven
 * @version : ThreadLocalUtils, v 0.1 2020-03-13 13:24 Deven Exp$
 */
public class LuceneThreadLocalUtils {

    private static ThreadLocal<IndexSearcher> indexSearcherThreadLocal = new ThreadLocal<>();


    public static IndexSearcher getIndexSearcher() {
        return indexSearcherThreadLocal.get();
    }

    public static void setIndexSearcher(IndexSearcher indexSearcher) {
        indexSearcherThreadLocal.set(indexSearcher);
    }

}
