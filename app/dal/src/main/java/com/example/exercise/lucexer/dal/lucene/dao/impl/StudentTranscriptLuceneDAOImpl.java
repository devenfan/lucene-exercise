package com.example.exercise.lucexer.dal.lucene.dao.impl;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.lucene.LuceneDalConfiguration;
import com.example.exercise.lucexer.dal.lucene.LuceneDalException;
import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import com.example.exercise.lucexer.dal.lucene.utils.LuceneThreadLocalUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.grouping.FirstPassGroupingCollector;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.SearchGroup;
import org.apache.lucene.search.grouping.SecondPassGroupingCollector;
import org.apache.lucene.search.grouping.TermGroupSelector;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.search.grouping.TopGroupsCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentTranscriptLuceneDAOImpl
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAOImpl, v 0.1 2020-03-11 00:20 Deven Exp$
 */
@Repository
public class StudentTranscriptLuceneDAOImpl extends AbstractLuceneDAO implements StudentTranscriptLuceneDAO {

    private final Logger                        logger               = LoggerFactory.getLogger(getClass());

    private static final int                    DEFAULT_RECORD_LIMIT = 1000;

    @Resource
    private StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper;

    @Override
    public List<StudentTranscriptLuceneDO> queryByFamilyName(String familyName) {
//        QueryParser qp = new QueryParser("name", analyzer);
//        Query query = null;
//        try {
//            query = qp.parse(familyName);
//        } catch (ParseException e) {
//            throw new LuceneDalException("queryByFamilyName Error", e);
//        }
        PrefixQuery query = new PrefixQuery(new Term("name", familyName));
        // 创建排序对象,需要排序字段SortField，参数：字段的名称、字段的类型、是否反转（true降序/false升序）
        Sort sort = new Sort(new SortField("studentId", SortField.Type.LONG, false));
        return doQueryDocs("queryByFamilyName", query, DEFAULT_RECORD_LIMIT, sort);
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryByAgeRange(int ageFrom, int ageTo) {
        Query query = IntPoint.newRangeQuery("age", ageFrom, ageTo);
        // 创建排序对象,需要排序字段SortField，参数：字段的名称、字段的类型、是否反转（true降序/false升序）
        Sort sort = new Sort(new SortField("age", SortField.Type.INT, false));
        return doQueryDocs("queryByAgeRange", query, DEFAULT_RECORD_LIMIT, sort);
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryByCityAreaAndScoreLimit(String cityArea, int scoreLimit) {
        TermQuery query1 = new TermQuery(new Term("cityArea", cityArea));
        Query query2 = DoublePoint.newRangeQuery("scoreLow", scoreLimit, 100);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query1, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST);
        Sort sort = new Sort(new SortField("studentId", SortField.Type.LONG, false));
        return doQueryDocs("queryByFamilyName", booleanQuery.build(), DEFAULT_RECORD_LIMIT, sort);
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryTop100ByProvinceAndSexAndHouseNumber(String province, String sex, String houseNumber) {
        TermQuery query1 = new TermQuery(new Term("province", province));
        TermQuery query2 = new TermQuery(new Term("sex", sex));
        WildcardQuery query3 = new WildcardQuery(new Term("houseNumber", "*" + houseNumber + "*"));
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query1, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST);
        booleanQuery.add(query3, BooleanClause.Occur.MUST);
        Sort sort = new Sort(new SortField("studentId", SortField.Type.LONG, false));
        return doQueryDocs("queryTop100ByProvinceAndSexAndHouseNumber", booleanQuery.build(), 100, sort);
    }



    @Override
    public Map<String, Long> summaryByFamilyName() {
        WildcardQuery query = new WildcardQuery(new Term("name", "*"));
        String groupByField = "familyName";
        String operationName = "summaryByFamilyName";
        try {
            IndexSearcher indexSearcher = LuceneThreadLocalUtils.getIndexSearcher();
            //分组查询
            TopGroups<BytesRef> topGroups = doGroupBy(indexSearcher, query, groupByField, 1000, 500000);
            if(topGroups == null) {
                return new HashMap<>(0);
            }
            Map<String, Long> result = new HashMap<>(topGroups.groups.length);
            GroupDocs<BytesRef>[] gds = topGroups.groups;
            for (GroupDocs<BytesRef> gd : gds) {
                result.put(gd.groupValue.utf8ToString(), gd.totalHits);
            }
            return result;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }
    }

    @Override
    public Map<String, Double> summaryByCityAndHuaxueFail() {
        Query query = DoublePoint.newRangeQuery("scoreHuaxue", 0, 59.9999999);
        String groupByField = "city";
        String operationName = "summaryByCityAndHuaxueFail";
        try {
            IndexSearcher indexSearcher = LuceneThreadLocalUtils.getIndexSearcher();
            long totalCount = doSelectCount(indexSearcher, query);
            if(totalCount == 0) {
                return new HashMap<>(0);
            }
            //分组查询
            TopGroups<BytesRef> topGroups = doGroupBy(indexSearcher, query, groupByField, 1000, 300000);
            if(topGroups == null) {
                return new HashMap<>(0);
            }
            Map<String, Double> result = new HashMap<>(topGroups.groups.length);
            GroupDocs<BytesRef>[] gds = topGroups.groups;
            for (GroupDocs<BytesRef> gd : gds) {
                long hitsInThisGroup = gd.totalHits;
                double percent = Math.round((hitsInThisGroup / totalCount) * 10000) / 10000;
                result.put(gd.groupValue.utf8ToString(), percent);
            }
            return result;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }

    }

    @Override
    public Map<String, Object> summaryScorePerformanceByVillaHouse() {
        String operationName = "summaryScorePerformanceByVillaHouse";
        int pageSize = 1000;
        try {
            Map<String, Object> result = new HashMap<>();
            IndexSearcher indexSearcher = LuceneThreadLocalUtils.getIndexSearcher();
            //查询所有同学
            Query queryAllStu = new WildcardQuery(new Term("name", "*"));
            //平均分超过85分
            Query queryGoodStu = DoublePoint.newRangeQuery("scoreAvg", 85, 100);
            Map<String, Long> map1 = new HashMap<>();
            Map<String, Long> map2 = new HashMap<>();
            Map<String, String> map3 = new HashMap<>();
            //按别墅分组查询所有学生
            TopGroups<BytesRef> topGroups1 = doGroupBy(indexSearcher, queryAllStu, "villaHouse", 1000, 1000000);
            GroupDocs<BytesRef>[] gds1 = topGroups1.groups;
            for (GroupDocs<BytesRef> gd : gds1) {
                String villaHouse = gd.groupValue.utf8ToString();
                map1.put(villaHouse, gd.totalHits);
            }
            //按别墅分组查询好学生
            TopGroups<BytesRef> topGroups2 = doGroupBy(indexSearcher, queryGoodStu, "villaHouse", 1000, 1000000);
            GroupDocs<BytesRef>[] gds2 = topGroups2.groups;
            for (GroupDocs<BytesRef> gd : gds2) {
                String villaHouse = gd.groupValue.utf8ToString();
                map2.put(villaHouse, gd.totalHits);
                long totalStuCount = map1.get(villaHouse);
                long goodStuCount = map2.get(villaHouse);
                String percent = 1.0 * goodStuCount / totalStuCount * 100 + "%";
                map3.put(villaHouse, percent);
            }

            map1.forEach((k, v) -> {
                if(k.equals("1")) {
                    result.put("住别墅的学生总人数", v);
                } else {
                    result.put("不住别墅的学生总人数", v);
                }
            });

            map2.forEach((k, v) -> {
                if(k.equals("1")) {
                    result.put("住别墅的高分学生人数", v);
                } else {
                    result.put("不住别墅的高分学生人数", v);
                }
            });

            map3.forEach((k, v) -> {
                if(k.equals("1")) {
                    result.put("住别墅的高分学生占比", v);
                } else {
                    result.put("不住别墅的高分学生占比", v);
                }
            });
            return result;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }
    }

    // Private ----------------------------------------------------------------------------------------

    private List<StudentTranscriptLuceneDO> doQueryDocs(String operationName, Query query, int limit, Sort sort) {
        try {
            IndexSearcher indexSearcher = LuceneThreadLocalUtils.getIndexSearcher();
            if(sort == null) {
                sort = new Sort(new SortField(null, SortField.Type.SCORE));
            }
            TopDocs docs = doSelectDocs(indexSearcher, query, limit, sort);
            List<StudentTranscriptLuceneDO> resultList = new ArrayList<>(docs.scoreDocs == null ? 0 : docs.scoreDocs.length);
            if (docs.scoreDocs != null) {
                for (ScoreDoc scoreDoc : docs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    StudentTranscriptLuceneDO luceneDO = studentTranscriptLuceneDomainMapper.doc2bean(document);
                    if (luceneDO != null) {
                        resultList.add(luceneDO);
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }
    }

}
