package com.example.exercise.lucexer.dal.lucene.utils;

import org.apache.lucene.search.IndexSearcher;

/**
 * LuceneIndexSearchUtils
 *
 * @author Deven
 * @version : LuceneIndexSearchUtils, v 0.1 2020-03-13 13:24 Deven Exp$
 */
public class LuceneIndexSearchUtils {

    private static ThreadLocal<IndexSearcher> indexSearcherThreadLocal = new ThreadLocal<>();

    public static IndexSearcher getIndexSearcherFromThreadLocal() {
        return indexSearcherThreadLocal.get();
    }

    public static void setIndexSearcherIntoThreadLocal(IndexSearcher indexSearcher) {
        indexSearcherThreadLocal.set(indexSearcher);
    }

}
