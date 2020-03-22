package com.example.exercise.lucexer.sync.search.service;

import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SearchServiceImpl
 *
 * @author Deven
 * @version : SearchServiceImpl, v 0.1 2020-03-15 13:02 Deven Exp$
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final int           DEFAULT_RECORD_LIMIT = 1000;

    @Resource
    private StudentTranscriptLuceneDAO studentTranscriptLuceneDAO;

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
        return studentTranscriptLuceneDAO.query("queryByFamilyName", query,  sort, DEFAULT_RECORD_LIMIT);
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryByAgeRange(int ageFrom, int ageTo) {
        Query query = IntPoint.newRangeQuery("age", ageFrom, ageTo);
        // 创建排序对象,需要排序字段SortField，参数：字段的名称、字段的类型、是否反转（true降序/false升序）
        Sort sort = new Sort(new SortField("age", SortField.Type.INT, false));
        return studentTranscriptLuceneDAO.query("queryByAgeRange", query, sort, DEFAULT_RECORD_LIMIT);
    }

    @Override
    public List<StudentTranscriptLuceneDO> queryByCityAreaAndScoreLimit(String cityArea, int scoreLimit) {
        TermQuery query1 = new TermQuery(new Term("cityArea", cityArea));
        Query query2 = DoublePoint.newRangeQuery("scoreLow", scoreLimit, 100);
        BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
        booleanQuery.add(query1, BooleanClause.Occur.MUST);
        booleanQuery.add(query2, BooleanClause.Occur.MUST);
        Sort sort = new Sort(new SortField("studentId", SortField.Type.LONG, false));
        return studentTranscriptLuceneDAO.query("queryByFamilyName", booleanQuery.build(), sort, DEFAULT_RECORD_LIMIT);
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
        return studentTranscriptLuceneDAO.query("queryTop100ByProvinceAndSexAndHouseNumber", booleanQuery.build(), sort, 100);
    }

    @Override
    public Map<String, Long> summaryByFamilyName() {
        WildcardQuery query = new WildcardQuery(new Term("name", "*"));
        String groupByField = "familyName";
        String operationName = "summaryByFamilyName";
        return studentTranscriptLuceneDAO.groupCountByField(operationName, query, groupByField, 1000, 500000);
    }

    @Override
    public Map<String, Object> summaryByCityAndHuaxueFail() {
        Query query1 = DoublePoint.newRangeQuery("scoreHuaxue", 0, 100);
        Query query2 = DoublePoint.newRangeQuery("scoreHuaxue", 0, 59.9999999);
        String groupByField = "city";
        String operationName = "summaryByCityAndHuaxueFail";
        //按城市分组统计所有学生
        Map<String, Long> allStuMap = studentTranscriptLuceneDAO.groupCountByField(operationName, query1, groupByField, 1000, 600000);
        //按城市分组统计化学不及格的学生
        Map<String, Long> failStuMap = studentTranscriptLuceneDAO.groupCountByField(operationName, query2, groupByField, 1000, 300000);
        //计算结果
        Map<String, Object> resultMap = new HashMap<>();
        allStuMap.forEach((k, v) -> {
            long totalStuCount = v;
            long failStuCount = failStuMap.get(k);
            String percent = 100.0 * failStuCount / totalStuCount + "%";
            resultMap.put(k + "化学不及格的学生占比", percent);
        });
        return resultMap;
    }

    @Override
    public Map<String, Object> summaryScorePerformanceByVillaHouse() {

        String operationName = "summaryScorePerformanceByVillaHouse";
        //查询所有同学
        Query queryAllStu = new WildcardQuery(new Term("name", "*"));
        //平均分超过85分
        Query queryGoodStu = DoublePoint.newRangeQuery("scoreAvg", 85, 100);
        //分组字段
        String groupByField = "villaHouse";
        //按别墅分组统计所有学生
        Map<String, Long> allStuMap = studentTranscriptLuceneDAO.groupCountByField(operationName, queryAllStu, groupByField, 1000, 1000000);
        //按别墅分组统计好学生
        Map<String, Long> goodStuMap = studentTranscriptLuceneDAO.groupCountByField(operationName, queryGoodStu, groupByField, 1000, 1000000);
        //计算结果
        Map<String, Object> resultMap = new HashMap<>();

        allStuMap.forEach((k, v) -> {
            long totalStuCount = v;
            long goodStuCount = goodStuMap.get(k);
            String percent = 100.0 * goodStuCount / totalStuCount + "%";
            if(k.equals("1")) {
                resultMap.put("住别墅的高分学生占比", percent);
            } else {
                resultMap.put("不住别墅的高分学生占比", percent);
            }
        });

        allStuMap.forEach((k, v) -> {
            if(k.equals("1")) {
                resultMap.put("住别墅的学生总人数", v);
            } else {
                resultMap.put("不住别墅的学生总人数", v);
            }
        });

        goodStuMap.forEach((k, v) -> {
            if(k.equals("1")) {
                resultMap.put("住别墅的高分学生人数", v);
            } else {
                resultMap.put("不住别墅的高分学生人数", v);
            }
        });

        return resultMap;
    }

}
