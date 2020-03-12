package com.example.exercise.lucexer.dal.lucene.dao;

import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;

import java.util.List;
import java.util.Map;

/**
 * StudentTranscriptLuceneDAO
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAO, v 0.1 2020-03-11 00:20 Deven Exp$
 */
public interface StudentTranscriptLuceneDAO {

    /**
     * 目标： 查询出 所有 姓"赵"的学生
     */
    List<StudentTranscriptLuceneDO> queryByFamilyName(String familyName);

    /**
     * 查询出 所有年龄在10-20的 并且从小到大排序
     */
    List<StudentTranscriptLuceneDO> queryByAgeRange(int ageFrom, int ageTo);

    /**
     * 查询出 所有住在"红星区"的，并且考试成绩有一门大于90分的学生列表。
     */
    List<StudentTranscriptLuceneDO> queryByCityAreaAndScoreLimit(String cityArea, int scoreLimit);

    /**
     * 查询出 各姓名的出现的数量 如: 王:100 ,赵:200
     */
    Map<String, Long> summaryByFamilyName();

    /**
     * 查询出 化学考试不及格的的人各城市的占比
     */
    Map<String, Double> summaryByCityAndHuaxueFail();

    /**
     * 查询出: 江苏省 ，门牌号是100的，前100个男的
     */
    List<StudentTranscriptLuceneDO> queryTop100BySexAndHouseNumber(String sex, String houseNumber);

    /**
     * 查询出: 住别墅的人的成绩是否比不住别墅的人好
     */
    Map<String, Double> summaryScorePerformanceByVillaHouse(boolean liveInVillaHouse);
}
