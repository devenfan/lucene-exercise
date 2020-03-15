package com.example.exercise.lucexer.sync;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentTranscriptDAO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentMaxMinIdInfoDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * IndexDataSyncService
 *
 * @author Deven
 * @version : IndexDataSyncService, v 0.1 2020-03-11 13:32 Deven Exp$
 */
@Service
public class IndexDataSyncService {

    private static final String                 ADDRESS_REX = "^(.*省){1}(.*市){1}(.*区){1}([^\\x00-\\xff]){0,}([0-9]\\d*)号(.*)";

    private static final int                    BATCH_SIZE = 10000;

    private final Logger                        logger      = LoggerFactory.getLogger(getClass());

    @Resource
    private StudentDAO                          studentDAO;

    @Resource
    private StudentTranscriptDAO                studentTranscriptDAO;

    @Resource
    private StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper;

    @Resource
    private IndexWriter                         indexWriter;

    private ThreadPoolExecutor                  threadPoolExecutor;

    private IndexDataSyncStatusData             lastSyncStatus;

    private IndexDataSyncStatusData             currentSyncStatus;

    public IndexDataSyncService() {
        lastSyncStatus = IndexDataSyncStatusData.loadFromFile();
        currentSyncStatus = lastSyncStatus;
        threadPoolExecutor = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }

    public boolean syncAll() {
        StudentMaxMinIdInfoDO studentMaxMinIdInfoDO = studentDAO.queryMaxMinIdInfo();
        return doSync(IndexDataSyncConstants.SYNC_TYPE_FULL, studentMaxMinIdInfoDO.getMinId(), studentMaxMinIdInfoDO.getMaxId(), true);
    }


    public boolean syncIncremental() {
        long fromStudentId = 0L;
        synchronized (this) {
            if(currentSyncStatus == null || !currentSyncStatus.getFinished()) {
                //没有全量同步过，或者正在同步中，都不能做增量同步
                logger.warn("不具备增量同步的条件！本次不执行同步！");
                return false;
            }
            fromStudentId = currentSyncStatus.getFinishStudentId();
        }
        StudentMaxMinIdInfoDO studentMaxMinIdInfoDO = studentDAO.queryMaxMinIdInfo();
        if(fromStudentId == studentMaxMinIdInfoDO.getMaxId().longValue()) {
            logger.warn("已同步完所有同学，无需再次同步！");
            return true;
        }
        return doSync(IndexDataSyncConstants.SYNC_TYPE_INCREMENTAL, fromStudentId, studentMaxMinIdInfoDO.getMaxId(), false);
    }

    /**
     * 做同步操作的入口方法
     */
    private boolean doSync(String syncType, long fromStudentId, long toStudentId, boolean byBatches) {
        if(beforeSync(syncType, fromStudentId, toStudentId)) {
            boolean syncSuccess = byBatches ? innerSyncByBatches(fromStudentId, toStudentId, BATCH_SIZE) : innerSyncOnce(fromStudentId, toStudentId);
            return afterSync(syncSuccess);
        }
        logger.warn("Cannot doSync right now!");
        return false;
    }

    private synchronized boolean beforeSync(String syncType, long fromStudentId, long toStudentId) {
        if(currentSyncStatus != null && !currentSyncStatus.getFinished()) {
            //同步中，不能再次发起
            logger.warn("有同步正在进行!");
            return false;
        }
        IndexDataSyncStatusData newSyncStatus = new IndexDataSyncStatusData();
        newSyncStatus.setFromStudentId(fromStudentId);
        newSyncStatus.setToStudentId(toStudentId);
        newSyncStatus.setSynchronizing(true);
        newSyncStatus.setFinished(false);
        newSyncStatus.setBeginTime(new Date());
        currentSyncStatus = newSyncStatus;
        return true;
    }

    private synchronized boolean afterSync(boolean syncSuccess) {
        currentSyncStatus.setSynchronizing(false);
        currentSyncStatus.setFinished(true);
        currentSyncStatus.setFinishTime(new Date());
        currentSyncStatus.setSuccess(syncSuccess);
        if(syncSuccess) {
            currentSyncStatus.setFinishStudentId(currentSyncStatus.getToStudentId());
            try {
                //写文件，写成功后把上一次状态更新一下
                IndexDataSyncStatusData.writeToFile(currentSyncStatus);
                logger.warn("Sync successfully!");
                lastSyncStatus = currentSyncStatus;
                return true;
            } catch (Exception ex) {
                //写文件失败，回滚至上一次状态
                logger.error("Persist status data failed, roll back to last one! Error: {}", ex.getMessage());
                currentSyncStatus = lastSyncStatus;
                return false;
            }
        } else {
            //同步失败，回滚至上一次状态
            logger.error("Sync failed, roll back to last one!");
            currentSyncStatus = lastSyncStatus;
            return false;
        }
    }

    private boolean innerSyncOnce(long fromStudentId, long toStudentId) {
        boolean result = true;
        Future future = submitTask(fromStudentId, toStudentId);
        try {
            future.get();
            result = true;
        } catch (InterruptedException | ExecutionException e) {
            result = false;
        }
        return result;
    }

    private boolean innerSyncByBatches(long fromStudentId, long toStudentId, int batchSize) {
        boolean result = true;
        long batchCount = (toStudentId - fromStudentId) / batchSize;
        List<Future> futures = new ArrayList<Future>((int)batchCount);
        logger.warn("syncByBatch from student-{} to student-{}, split into {} batches", fromStudentId, toStudentId, batchCount);
        for(long i = 0, curFrom = fromStudentId; i < batchCount; i++) {
            long curTo = curFrom + batchSize;
            Future future = submitTask(curFrom, curTo);
            futures.add(future);
            curFrom = curTo;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                result = false;
            }
        }
        return result;
    }

    private Future submitTask(long fromStudentId, long toStudentId) {
        logger.warn("execute task begin, from student-{} to student-{}", fromStudentId, toStudentId);
        return threadPoolExecutor.submit( () -> {
            try {
                indexWriter.maybeMerge();
                List<StudentTranscriptDO> studentTranscriptDOS = studentTranscriptDAO.selectByStudentRange(fromStudentId, toStudentId);
                for (StudentTranscriptDO studentTranscriptDO : studentTranscriptDOS) {
                    StudentTranscriptLuceneDO luceneDO = transform(studentTranscriptDO);
                    Document document = studentTranscriptLuceneDomainMapper.domain2Doc(luceneDO);
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
