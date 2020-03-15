package com.example.exercise.lucexer.sync;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * IndexDataSyncStatusData
 *
 * @author Deven
 * @version : IndexDataSyncStatusData, v 0.1 2020-03-14 16:52 Deven Exp$
 */
public class IndexDataSyncStatusData {

    /**
     * 本次同步的起始学生id
     */
    private Long fromStudentId;

    /**
     * 本次同步的中止学生id
     */
    private Long toStudentId;

    /**
     * 同步完成后的最大学生id
     */
    private Long finishStudentId;

    /**
     * 本次同步的数量
     */
    private Long syncCount;

    /**
     * sync type: INCREMENTAL, FULL
     */
    private String syncType;

    /**
     * 是否正在同步
     */
    private Boolean synchronizing;

    /**
     * 本次同步是否已完成
     */
    private Boolean finished = false;

    /**
     * 是否同步成功
     */
    private Boolean success = false;

    /**
     * 同步开始时间(yyyy-MM-dd HH:mm:ss.SSS)
     */
    private Date beginTime;

    /**
     * 同步结束时间(yyyy-MM-dd HH:mm:ss.SSS)
     */
    private Date finishTime;

    public Long getFromStudentId() {
        return fromStudentId;
    }

    public void setFromStudentId(Long fromStudentId) {
        this.fromStudentId = fromStudentId;
    }

    public Long getToStudentId() {
        return toStudentId;
    }

    public void setToStudentId(Long toStudentId) {
        this.toStudentId = toStudentId;
    }

    public Long getFinishStudentId() {
        return finishStudentId;
    }

    public void setFinishStudentId(Long finishStudentId) {
        this.finishStudentId = finishStudentId;
    }

    public Long getSyncCount() {
        return syncCount;
    }

    public void setSyncCount(Long syncCount) {
        this.syncCount = syncCount;
    }

    public String getSyncType() {
        return syncType;
    }

    public void setSyncType(String syncType) {
        this.syncType = syncType;
    }

    public Boolean getSynchronizing() {
        return synchronizing;
    }

    public void setSynchronizing(Boolean synchronizing) {
        this.synchronizing = synchronizing;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }


    private static final String FILE_NAME = "indexSyncStatus.properties";

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static IndexDataSyncStatusData loadFromFile() {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(FILE_NAME)) {
            properties.load(in);
            return fromProperties(properties);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void writeToFile(IndexDataSyncStatusData statusData) {
        Properties properties = toProperties(statusData);
        try (FileOutputStream oFile = new FileOutputStream(FILE_NAME)) {
            properties.store(oFile, "索引同步状态文件");
        } catch (Exception ex) {
            throw new RuntimeException("Cannot persist " + FILE_NAME, ex);
        }
    }

    public static IndexDataSyncStatusData fromProperties(Properties properties) {
        IndexDataSyncStatusData statusData = new IndexDataSyncStatusData();
        statusData.setFromStudentId(getLongValueFromProperties(properties, "fromStudentId"));
        statusData.setToStudentId(getLongValueFromProperties(properties, "toStudentId"));
        statusData.setFinishStudentId(getLongValueFromProperties(properties, "finishStudentId"));
        statusData.setSyncCount(getLongValueFromProperties(properties, "syncCount"));
        statusData.setSyncType(getStringValueFromProperties(properties, "syncType"));
        statusData.setSynchronizing(getBooleanValueFromProperties(properties, "synchronizing"));
        statusData.setFinished(getBooleanValueFromProperties(properties, "finished"));
        statusData.setSuccess(getBooleanValueFromProperties(properties, "success"));
        statusData.setBeginTime(getDateValueFromProperties(properties, "beginTime"));
        statusData.setFinishTime(getDateValueFromProperties(properties, "finishTime"));
        return statusData;
    }

    public static Properties toProperties(IndexDataSyncStatusData statusData) {
        Properties properties = new Properties();
        setValueIntoProperties("fromStudentId", statusData.getFromStudentId(), properties);
        setValueIntoProperties("toStudentId", statusData.getToStudentId(), properties);
        setValueIntoProperties("finishStudentId", statusData.getFinishStudentId(), properties);
        setValueIntoProperties("syncCount", statusData.getSyncCount(), properties);
        setValueIntoProperties("syncType", statusData.getSyncType(), properties);
        setValueIntoProperties("synchronizing", statusData.getSynchronizing(), properties);
        setValueIntoProperties("finished", statusData.getFinished(), properties);
        setValueIntoProperties("success", statusData.getSuccess(), properties);
        setValueIntoProperties("beginTime", statusData.getBeginTime(), properties);
        setValueIntoProperties("finishTime", statusData.getFinishTime(), properties);
        return properties;
    }

    private static Long getLongValueFromProperties(Properties properties, String key) {
        Object value = properties.get(key);
        return value == null ? null : Long.valueOf(value.toString());
    }

    private static Integer getIntegerValueFromProperties(Properties properties, String key) {
        Object value = properties.get(key);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    private static Boolean getBooleanValueFromProperties(Properties properties, String key) {
        Object value = properties.get(key);
        return value == null ? null : Boolean.valueOf(value.toString());
    }

    private static String getStringValueFromProperties(Properties properties, String key) {
        Object value = properties.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Date getDateValueFromProperties(Properties properties, String key) {
        Object value = properties.get(key);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return value == null ? null : simpleDateFormat.parse(value.toString());
        } catch (ParseException e) {
            return null;
        }
    }

    private static void setValueIntoProperties(String key, Object value, Properties properties) {
        if(value == null) {
            return;
        }
        if(value instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            properties.setProperty(key, simpleDateFormat.format(value));
        } else {
            properties.setProperty(key, value.toString());
        }
    }
}
