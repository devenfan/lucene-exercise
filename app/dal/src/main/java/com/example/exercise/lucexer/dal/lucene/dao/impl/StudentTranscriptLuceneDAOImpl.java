package com.example.exercise.lucexer.dal.lucene.dao.impl;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.lucene.LuceneDalConfiguration;
import com.example.exercise.lucexer.dal.lucene.LuceneDalException;
import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * StudentTranscriptLuceneDAOImpl
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAOImpl, v 0.1 2020-03-11 00:20 Deven Exp$
 */
@Repository
public class StudentTranscriptLuceneDAOImpl implements StudentTranscriptLuceneDAO {

    private final Logger                        logger               = LoggerFactory.getLogger(getClass());

    private static final int                    DEFAULT_RECORD_LIMIT = 1000;

    @Resource
    private Directory                           indexDirectory;

    @Resource
    private Analyzer                            analyzer;

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
    public Map<String, Long> summaryByFamilyName() {
        return null;
    }

    @Override
    public Map<String, Double> summaryByCityAndHuaxueFail() {
        return null;
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryTop100BySexAndHouseNumber(String sex, String houseNumber) {
        TermQuery query1 = new TermQuery(new Term("sex", sex));
        WildcardQuery query2 = new WildcardQuery(new Term("houseNumber", "*" + houseNumber + "*"));
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query1, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST);
        Sort sort = new Sort(new SortField("studentId", SortField.Type.LONG, false));
        return doQueryDocs("queryByFamilyName", booleanQuery.build(), 100, sort);
    }

    @Override
    public Map<String, Double> summaryScorePerformanceByVillaHouse(boolean liveInVillaHouse) {
        return null;
    }


    private List<StudentTranscriptLuceneDO> doQueryDocs(String operationName, Query query, int limit, Sort sort) {
        try(IndexReader indexReader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            TopDocs docs = indexSearcher.search(query, limit, sort);
            List<StudentTranscriptLuceneDO> resultList = new ArrayList<>(docs.scoreDocs == null ? 0 : docs.scoreDocs.length);
            if (docs.scoreDocs != null) {
                for (ScoreDoc scoreDoc : docs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    StudentTranscriptLuceneDO luceneDO = studentTranscriptLuceneDomainMapper.doc2bean(document);
                    if(luceneDO != null) {
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
