package com.example.exercise.lucexer.sync;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import com.example.exercise.lucexer.dal.mybatis.dao.CourseDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentTranscriptDAO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentMaxMinIdInfoDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * IndexDataSyncManager
 *
 * @author Deven
 * @version : IndexDataSyncManager, v 0.1 2020-03-11 13:32 Deven Exp$
 */
@Service
public class IndexDataSyncManager {

    private static final String                 ADDRESS_REX = "^(.*省){1}(.*市){1}(.*区){1}([^\\x00-\\xff]){0,}([0-9]\\d*)号(.*)";

    private final Logger                        logger      = LoggerFactory.getLogger(getClass());

    @Resource
    private StudentDAO                          studentDAO;

    @Resource
    private CourseDAO                           courseDAO;

    @Resource
    private StudentTranscriptDAO                studentTranscriptDAO;

    @Resource
    private StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper;

    @Resource
    private IndexWriter                         indexWriter;

    private ThreadPoolExecutor                  threadPoolExecutor;

    public IndexDataSyncManager() {
        threadPoolExecutor = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }

    public void syncAll() {
        StudentMaxMinIdInfoDO studentMaxMinIdInfoDO = studentDAO.queryMaxMinIdInfo();
        int batchSize = 10000;
        long batchCount = studentMaxMinIdInfoDO.getTotalCount() / batchSize;
        List<Future> futures = new ArrayList<Future>((int)batchCount);
        logger.warn("Total {} students, from student-{} to student-{}", studentMaxMinIdInfoDO.getTotalCount(), studentMaxMinIdInfoDO.getMinId(), studentMaxMinIdInfoDO.getMaxId());
        for(long i = 0, curStudentId = studentMaxMinIdInfoDO.getMinId(); i < batchCount; i++) {
            long fromStudentId = curStudentId;
            long toStudentId = curStudentId + batchSize;
            Future future = submitTask(fromStudentId, toStudentId);
            futures.add(future);
            curStudentId = toStudentId;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    public void syncRange(long fromStudentId, long toStudentId) {
        Future future = submitTask(fromStudentId, toStudentId);
        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Future submitTask(long fromStudentId, long toStudentId) {
        logger.warn("execute task ready, from student-{} to student-{}", fromStudentId, toStudentId);
        return threadPoolExecutor.submit( () -> {
            try {
                indexWriter.maybeMerge();
                List<StudentTranscriptDO> studentTranscriptDOS = studentTranscriptDAO.selectByStudentRange(fromStudentId, toStudentId);
                for (StudentTranscriptDO studentTranscriptDO : studentTranscriptDOS) {
                    StudentTranscriptLuceneDO luceneDO = transform(studentTranscriptDO);
                    Document document = studentTranscriptLuceneDomainMapper.bean2doc(luceneDO);
                    indexWriter.addDocument(document);
                }
                indexWriter.commit();
                logger.warn("execute task success, from student-{} to student-{}", fromStudentId, toStudentId);
            } catch (Exception ex) {
                logger.error("execute task error, from student-{} to student-{}", fromStudentId, toStudentId);
                logger.error(ex.toString());
            }
        });
    }

    private StudentTranscriptLuceneDO transform(StudentTranscriptDO studentTranscriptDO) {
        StudentTranscriptLuceneDO luceneDO = new StudentTranscriptLuceneDO();
        luceneDO.setStudentId(studentTranscriptDO.getStudentId());
        luceneDO.setName(studentTranscriptDO.getName());
        //TODO: 处理复姓
        luceneDO.setFamilyName(studentTranscriptDO.getName().substring(0, 1));
        luceneDO.setAge(studentTranscriptDO.getAge());
        luceneDO.setSex(studentTranscriptDO.getAge() == null ? "" : (studentTranscriptDO.getAge() == 1 ? "男" : "女"));
        luceneDO.setGrade(studentTranscriptDO.getGrade());
        luceneDO.setAddress(studentTranscriptDO.getAddress());

        //TODO: 处理小区名
        List<String> addrList = ReUtil.getAllGroups(PatternPool.get(ADDRESS_REX, Pattern.DOTALL), studentTranscriptDO.getAddress());
        String province = addrList.get(1);
        String city = addrList.get(2);
        String cityArea = addrList.get(3);
        String houseNumber = addrList.get(5);
        String village = addrList.get(6);
        luceneDO.setProvince(province);
        luceneDO.setCity(city);
        luceneDO.setCityArea(cityArea);
        luceneDO.setHouseNumber(houseNumber);
        luceneDO.setVillage(village);
        luceneDO.setVillaHouse(village.contains("别墅") ? "1" : "0");

        Double highScore = 0d;
        Double lowScore = 0d;
        Double avgScore = 0d;
        Double sumScore = 0d;
        //TODO: 动态处理其他科目的分数
        if (StringUtils.isNotBlank(studentTranscriptDO.getCourseNames())) {
            String[] courseNames = studentTranscriptDO.getCourseNames().split(StudentTranscriptDO.GROUP_CONCAT_SEPARATOR);
            String[] courseScores = studentTranscriptDO.getCourseScores().split(StudentTranscriptDO.GROUP_CONCAT_SEPARATOR);
            for (int i = 0; i < courseNames.length; i++) {
                String courseName = courseNames[i];
                Double courseScore = Double.parseDouble(courseScores[i]);
                sumScore += courseScore;
                if(i == 0) {
                    lowScore = courseScore;
                    highScore = courseScore;
                } else {
                    if (highScore < courseScore) {
                        highScore = courseScore;
                    }
                    if (lowScore > courseScore) {
                        lowScore = courseScore;
                    }
                }
                if (i == (courseNames.length - 1)) {
                    avgScore = sumScore / courseNames.length;
                    avgScore = (double) Math.round(avgScore * 100) / 100 ;
                }
                if (StringUtils.equals(courseName, IndexDataSyncConstants.COURSE_NAME_YUWEN)) {
                    luceneDO.setScoreYuwen(courseScore);
                } else if (StringUtils.equals(courseName, IndexDataSyncConstants.COURSE_NAME_SHUXUE)) {
                    luceneDO.setScoreShuxue(courseScore);
                } else if (StringUtils.equals(courseName, IndexDataSyncConstants.COURSE_NAME_HUAXUE)) {
                    luceneDO.setScoreHuaxue(courseScore);
                }
            }
        }
        luceneDO.setScoreHigh(highScore);
        luceneDO.setScoreLow(lowScore);
        luceneDO.setScoreSum(sumScore);
        luceneDO.setScoreAvg(avgScore);
        return luceneDO;
    }
}
