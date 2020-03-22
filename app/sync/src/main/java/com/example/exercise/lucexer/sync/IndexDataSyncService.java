package com.example.exercise.lucexer.sync;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.exercise.lucexer.common.IndexDataSyncConstants;
import com.example.exercise.lucexer.common.IndexDataSyncStatusData;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentTranscriptDAO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentMaxMinIdInfoDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;

/**
 * IndexDataSyncService
 *
 * @author Deven
 * @version : IndexDataSyncService, v 0.1 2020-03-11 13:32 Deven Exp$
 */
@Service
public class IndexDataSyncService implements InitializingBean, DisposableBean {

    private static final String                 ADDRESS_REX       = "^(.*省){1}(.*市){1}(.*区){1}([^\\x00-\\xff]){0,}([0-9]\\d*)号(.*)";

    private static final int                    BATCH_SIZE        = 10000;

    private final Logger                        logger            = LoggerFactory.getLogger(getClass());

    /**
     * 当前的索引主目录
     */
    @Value("${lucene.index.dir.main}")
    private String                              mainDir           = "c:/temp/lucene7index/exercise/index";

    /**
     * 当前的索引的目录轮换标识
     */
    @Value("${lucene.index.dir.rotateFlag}")
    private String                              currentRotateFlag = "A";

    /**
     * 状态文件所在目录
     */
    @Value("${lucene.syncStatus.fileDir}")
    private String                              syncStatusFileDir = "c:/temp/lucene7index/exercise/status";

    /**
     * 状态文件名
     */
    @Value("${lucene.syncStatus.fileName}")
    private String                              syncStatusFileName = "indexSyncStatus.properties";

    @Resource
    private StudentDAO                          studentDAO;

    @Resource
    private StudentTranscriptDAO                studentTranscriptDAO;

    @Resource
    private StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper;

    private ThreadPoolExecutor                  threadPoolExecutor;

    private IndexDataSyncStatusData             lastSyncStatus;

    private IndexDataSyncStatusData             currentSyncStatus;


    @Override
    public void afterPropertiesSet() throws Exception {
        lastSyncStatus = IndexDataSyncStatusData.loadFromFile(getSyncStatusFilePath());
        currentSyncStatus = lastSyncStatus;
        if (currentSyncStatus != null) {
            this.currentRotateFlag = currentSyncStatus.getRotateFlagAfterSync();
        }
        threadPoolExecutor = new ThreadPoolExecutor(10, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100));
    }

    @Override
    public void destroy() throws Exception {
        threadPoolExecutor.shutdown();
    }

    public boolean syncAll() {
        StudentMaxMinIdInfoDO studentMaxMinIdInfoDO = studentDAO.queryMaxMinIdInfo();
        return doSync(IndexDataSyncConstants.SYNC_TYPE_FULL, studentMaxMinIdInfoDO.getMinId(), studentMaxMinIdInfoDO.getMaxId(), true);
    }

    public boolean syncIncremental() {
        long fromStudentId = 0L;
        synchronized (this) {
            if (currentSyncStatus == null || !currentSyncStatus.getFinished()) {
                //没有全量同步过，或者正在同步中，都不能做增量同步
                logger.warn("不具备增量同步的条件！本次不执行同步！");
                return false;
            }
            fromStudentId = currentSyncStatus.getFinishStudentId();
        }
        StudentMaxMinIdInfoDO studentMaxMinIdInfoDO = studentDAO.queryMaxMinIdInfo();
        if (fromStudentId == studentMaxMinIdInfoDO.getMaxId().longValue()) {
            logger.warn("已同步完所有数据，无需再次同步！");
            return true;
        }
        return doSync(IndexDataSyncConstants.SYNC_TYPE_INCREMENTAL, fromStudentId, studentMaxMinIdInfoDO.getMaxId(), false);
    }

    /**
     * 做同步操作的入口方法
     */
    private boolean doSync(String syncType, long fromStudentId, long toStudentId, boolean byBatches) {
        if (beforeSync(syncType, fromStudentId, toStudentId)) {
            logger.warn("Let's doSync({}). currentSyncStatus: {}", syncType, currentSyncStatus);
            boolean isIncremental = IndexDataSyncConstants.SYNC_TYPE_INCREMENTAL.equals(syncType);
            String savePath = currentSyncStatus.getSavePath();
            boolean syncSuccess = byBatches ? innerSyncByBatches(isIncremental, savePath, fromStudentId, toStudentId, BATCH_SIZE)
                : innerSyncOnce(isIncremental, savePath, fromStudentId, toStudentId);
            return afterSync(syncSuccess);
        }
        logger.warn("Cannot doSync({}) right now!", syncType);
        return false;
    }

    private synchronized boolean beforeSync(String syncType, long fromStudentId, long toStudentId) {
        if (currentSyncStatus != null && !currentSyncStatus.getFinished()) {
            //同步中，不能再次发起
            logger.warn("有同步正在进行，不能再次发起!");
            return false;
        }
        boolean isFullSync = IndexDataSyncConstants.SYNC_TYPE_FULL.equals(syncType);
        IndexDataSyncStatusData newSyncStatus = new IndexDataSyncStatusData();
        newSyncStatus.setSyncUID(IndexDataSyncStatusData.generateSyncUID());
        newSyncStatus.setSyncType(syncType);
        newSyncStatus.setRotateFlagAfterSync(decideRotateFlagAfterSync(this.currentRotateFlag, isFullSync));
        newSyncStatus.setSavePath(this.mainDir + "/" + newSyncStatus.getRotateFlagAfterSync());
        newSyncStatus.setFromStudentId(fromStudentId);
        newSyncStatus.setToStudentId(toStudentId);
        newSyncStatus.setFinished(false);
        newSyncStatus.setBeginTime(new Date());
        currentSyncStatus = newSyncStatus;
        return true;
    }

    private synchronized boolean afterSync(boolean syncSuccess) {
        currentSyncStatus.setFinished(true);
        currentSyncStatus.setFinishTime(new Date());
        currentSyncStatus.setSuccess(syncSuccess);
        if (syncSuccess) {
            currentSyncStatus.setFinishStudentId(currentSyncStatus.getToStudentId());
            try {
                //写文件，写成功后把上一次状态更新一下
                IndexDataSyncStatusData.writeToFile(currentSyncStatus, getSyncStatusFilePath());
                lastSyncStatus = currentSyncStatus;
                currentRotateFlag = currentSyncStatus.getRotateFlagAfterSync();
                logger.warn("Sync successfully!");
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

    private boolean innerSyncOnce(boolean incrementalSync, String syncPath, long fromStudentId, long toStudentId) {
        boolean result = false;
        IndexWriter writer = null;
        //增量同步时，直接在查询索引上进行append
        boolean isAppend = incrementalSync;
        IndexWriterConfig.OpenMode writeOpenMode = isAppend ? IndexWriterConfig.OpenMode.APPEND : IndexWriterConfig.OpenMode.CREATE;
        logger.warn("innerSyncOnce from student-{} to student-{}, mode: {}, savePath: {}", fromStudentId, toStudentId, syncPath);
        try (Directory directory = FSDirectory.open(getPath(syncPath))) {
            IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
            config.setUseCompoundFile(true);
            config.setOpenMode(writeOpenMode);
            writer = new IndexWriter(directory, config);
            if (!isAppend) {
                //如果重新构建索引，则删除所有数据
                writer.deleteAll();
            }
            //do it
            Future future = submitTask(writer, fromStudentId, toStudentId);
            try {
                future.get();
                result = true;
            } catch (InterruptedException | ExecutionException e) {
                result = false;
            }
            return result;
        } catch (Exception ex) {
            logger.error("innerSyncOnce error: {}", ex.getMessage());
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }

    private boolean innerSyncByBatches(boolean incrementalSync, String syncPath, long fromStudentId, long toStudentId, int batchSize) {
        boolean result = false;
        long batchCount = (toStudentId - fromStudentId) / batchSize;
        List<Future> futures = new ArrayList<Future>((int) batchCount);
        IndexWriter writer = null;
        //增量同步时，直接在查询索引上进行append
        boolean isAppend = incrementalSync;
        IndexWriterConfig.OpenMode writeOpenMode = isAppend ? IndexWriterConfig.OpenMode.APPEND : IndexWriterConfig.OpenMode.CREATE;
        logger.warn("syncByBatch from student-{} to student-{}, split into {} batches, mode: {}, savePath: {}", fromStudentId, toStudentId, batchCount, writeOpenMode, syncPath);
        try (Directory directory = FSDirectory.open(getPath(syncPath))) {
            IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
            config.setUseCompoundFile(true);
            config.setOpenMode(writeOpenMode);
            writer = new IndexWriter(directory, config);
            if (!isAppend) {
                //如果重新构建索引，则删除所有数据
                writer.deleteAll();
            }
            //do it
            for (long i = 0, curFrom = fromStudentId; i < batchCount; i++) {
                long curTo = curFrom + batchSize;
                Future future = submitTask(writer, curFrom, curTo);
                futures.add(future);
                curFrom = curTo;
            }
            result = true;
            for (Future future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    result = false;
                }
            }
            return result;
        } catch (Exception ex) {
            logger.error("syncByBatch error: {}", ex.getMessage());
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }

    private boolean appendIndex(String appendIndexDir) {
        Path indexDirPath = getPath(getCurrentIndexPath());
        Path appendIndexDirPath = getPath(appendIndexDir);
        IndexWriter indexWriter = null;
        try (Directory directory = FSDirectory.open(indexDirPath)) {
            IndexWriterConfig config = new IndexWriterConfig(new SmartChineseAnalyzer());
            config.setUseCompoundFile(true);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(directory, config);
            indexWriter.addIndexes(FSDirectory.open(appendIndexDirPath));
            indexWriter.commit();
            logger.warn("mergeIndex from {} into {} success", appendIndexDirPath, indexDirPath);
            return true;
        } catch (Exception ex) {
            logger.error("mergeIndex from {} into {} error: {}", appendIndexDirPath, indexDirPath, ex.getMessage());
            return false;
        } finally {
            if (indexWriter != null) {
                try {
                    indexWriter.close();
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
    }

    private Future submitTask(IndexWriter indexWriter, long fromStudentId, long toStudentId) {
        logger.warn("execute task begin, from student-{} to student-{}", fromStudentId, toStudentId);
        return threadPoolExecutor.submit(() -> {
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
                if (i == 0) {
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
                    avgScore = (double) Math.round(avgScore * 100) / 100;
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

    private Path getPath(String pathStr) {
        Path path = Paths.get(pathStr);
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public String getSyncStatusFilePath() {
        return syncStatusFileDir + "/" + syncStatusFileName;
    }

    private String getCurrentIndexPath() {
        return mainDir + "/" + currentRotateFlag;
    }

    private String decideRotateFlagAfterSync(String rotateFlagBeforeSync, boolean isFullSync) {
        return isFullSync ? rotate(rotateFlagBeforeSync) : rotateFlagBeforeSync;
    }

    /**
     * 对rotateFlag进行翻转，之前为A则翻转后为B，之前为B则翻转后为A
     */
    private String rotate(String rotateFlag) {
        return "A".equals(rotateFlag) ? "B" : "A";
    }
}
