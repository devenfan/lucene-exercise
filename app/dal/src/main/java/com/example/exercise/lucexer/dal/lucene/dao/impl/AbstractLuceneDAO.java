package com.example.exercise.lucexer.dal.lucene.dao.impl;

import com.example.exercise.lucexer.dal.lucene.LuceneDalException;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CachingCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.FirstPassGroupingCollector;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.TermGroupSelector;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.TopGroupsCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AbstractLuceneDAO
 *
 * @author Deven
 * @version : AbstractLuceneDAO, v 0.1 2020-03-13 12:56 Deven Exp$
 */
public abstract class AbstractLuceneDAO {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    protected Directory indexDirectory;

    @Resource
    protected Analyzer analyzer;


    protected long doSelectCount(IndexSearcher indexSearcher, Query query) throws IOException {
        return indexSearcher.count(query);
    }

    protected TopDocs doSelectDocs(IndexSearcher indexSearcher, Query query, int limit, Sort sort) throws IOException {
        return indexSearcher.search(query, limit, sort);
    }

    protected TopGroups<BytesRef> doGroupBy(IndexSearcher indexSearcher, Query query, String groupByField, int topNGroups, int maxDocsPerGroup) throws IOException {
        int groupOffset = 0;
        int withinGroupOffset = 0;
        FirstPassGroupingCollector c1 = new FirstPassGroupingCollector(new TermGroupSelector(groupByField), Sort.RELEVANCE, topNGroups);
        boolean cacheScores = true;
        double maxCacheRAMMB = 4.0;
        //Caches all docs, and optionally also scores, coming from a search, and is then able to replay them to another collector.
        CachingCollector cachedCollector = CachingCollector.create(c1, cacheScores, maxCacheRAMMB);
//            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        indexSearcher.search(query, cachedCollector);
        Collection<SearchGroup<BytesRef>> tg1 = c1.getTopGroups(groupOffset, true);
        if (tg1 == null) {
            return null;
        }
        //TopGroupsCollector is A second-pass collector that collects the TopDocs for each group, and returns them as a TopGroups object
        TopGroupsCollector c2 = new TopGroupsCollector(new TermGroupSelector(groupByField), tg1, Sort.RELEVANCE, Sort.RELEVANCE, maxDocsPerGroup, false, false, false);
        if (cachedCollector.isCached()) {
            // Cache fit within maxCacheRAMMB, so we can replay it:
            cachedCollector.replay(c2);
        } else {
            // Cache was too large; must re-execute query:
            indexSearcher.search(query, c2);
        }

        TopGroups<BytesRef> tg2 = c2.getTopGroups(withinGroupOffset);
        //            GroupDocs<BytesRef>[] gds = tg2.groups;
        //            for(GroupDocs<BytesRef> gd : gds) {
        //                map.put(gd.groupValue, gd.totalHits);
        //            }
        return tg2;
    }
}
