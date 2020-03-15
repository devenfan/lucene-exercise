package com.example.exercise.lucexer.dal.lucene.dao;

import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;

/**
 * StudentTranscriptLuceneDAO
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAO, v 0.1 2020-03-11 00:20 Deven Exp$
 */
public interface StudentTranscriptLuceneDAO {

    /**
     * 查询实体列表
     */
    List<StudentTranscriptLuceneDO> query(String operationName, Query query, Sort sort, int limit);

    /**
     * 查询计数
     */
    long count(Query filterQuery);

    /**
     * 分组计数统计
     */
    Map<String, Long> groupCountByField(String operationName, Query filterQuery, String groupByField, int maxGroups, int maxDocsPerGroup);

    /**
     * 更新单个LuceneDO
     */
    boolean update(StudentTranscriptLuceneDO luceneDO);

    /**
     * 更新多个LuceneDO
     */
    boolean update(List<StudentTranscriptLuceneDO> luceneDOS);

    /**
     * 删除单个LuceneDO
     */
    boolean delete(StudentTranscriptLuceneDO luceneDO);

    /**
     * 删除多个LuceneDO
     */
    boolean delete(List<StudentTranscriptLuceneDO> luceneDOS);

}
