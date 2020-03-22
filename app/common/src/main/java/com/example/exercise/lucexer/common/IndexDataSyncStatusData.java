package com.example.exercise.lucexer.common;

import cn.hutool.core.lang.UUID;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * 本次同步的唯一标识
     */
    private String  syncUID;

    /**
     * 本次同步的保存目录
     */
    private String  savePath;

    /**
     * 本次同步后的目录轮换标识
     */
    private String  rotateFlagAfterSync;

    /**
     * 本次同步的起始学生id
     */
    private Long    fromStudentId;

    /**
     * 本次同步的中止学生id
     */
    private Long    toStudentId;

    /**
     * 同步完成后的最大学生id
     */
    private Long    finishStudentId;

    /**
     * 本次同步的数量
     */
    private Long    syncCount;

    /**
     * sync type: INCREMENTAL, FULL
     */
    private String  syncType;

    /**
     * 本次同步是否已完成
     */
    private Boolean finished = false;

    /**
     * 是否同步成功
     */
    private Boolean success  = false;

    /**
     * 同步开始时间(yyyy-MM-dd HH:mm:ss.SSS)
     */
    private Date    beginTime;

    /**
     * 同步结束时间(yyyy-MM-dd HH:mm:ss.SSS)
     */
    private Date    finishTime;

    public String getSyncUID() {
        return syncUID;
    }

    public void setSyncUID(String syncUID) {
        this.syncUID = syncUID;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getRotateFlagAfterSync() {
        return rotateFlagAfterSync;
    }

    public void setRotateFlagAfterSync(String rotateFlagAfterSync) {
        this.rotateFlagAfterSync = rotateFlagAfterSync;
    }

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

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    private static final String DATE_UID_FORMAT = "yyyyMMddHHmmssSSS";

    public static IndexDataSyncStatusData loadFromFile(String filePath) {
        return loadFromFile(new File(filePath));
    }

    public static IndexDataSyncStatusData loadFromFile(File file) {
        FileInputStream inFile = null;
        try {
            if(!file.exists()) {
                return null;
            }
            Properties properties = new Properties();
            inFile = new FileInputStream(file);
            properties.load(inFile);
            IndexDataSyncStatusData data = fromProperties(properties);
            if(!IndexDataSyncStatusData.validate(data)) {
                //invalid file, overwrite
                return null;
            }
            return data;
        } catch (Exception ex) {
            return null;
        }
    }

    public static void writeToFile(IndexDataSyncStatusData statusData, String filePath) {
        FileOutputStream oFile = null;
        try {
            File dir = new File(filePath.substring(0, filePath.lastIndexOf("/")));
            if(!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(filePath);
            if(!file.exists()) {
                file.createNewFile();
            }
            Properties properties = toProperties(statusData);
            oFile = new FileOutputStream(file);
            properties.store(oFile, "IndexDataSyncStatus File (Please DO NOT edit unless you known why)");
        } catch (Exception ex) {
            throw new RuntimeException("Cannot persist IndexDataSyncStatus to File: " + filePath, ex);
        } finally {
            if(oFile != null) {
                try {
                    oFile.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public static IndexDataSyncStatusData fromProperties(Properties properties) {
        IndexDataSyncStatusData statusData = new IndexDataSyncStatusData();
        statusData.setSyncUID(getStringValueFromProperties(properties, "syncUID"));
        statusData.setSavePath(getStringValueFromProperties(properties, "savePath"));
        statusData.setRotateFlagAfterSync(getStringValueFromProperties(properties, "rotateFlagAfterSync"));
        statusData.setFromStudentId(getLongValueFromProperties(properties, "fromStudentId"));
        statusData.setToStudentId(getLongValueFromProperties(properties, "toStudentId"));
        statusData.setFinishStudentId(getLongValueFromProperties(properties, "finishStudentId"));
        statusData.setSyncCount(getLongValueFromProperties(properties, "syncCount"));
        statusData.setSyncType(getStringValueFromProperties(properties, "syncType"));
        statusData.setFinished(getBooleanValueFromProperties(properties, "finished"));
        statusData.setSuccess(getBooleanValueFromProperties(properties, "success"));
        statusData.setBeginTime(getDateValueFromProperties(properties, "beginTime"));
        statusData.setFinishTime(getDateValueFromProperties(properties, "finishTime"));
        return statusData;
    }

    public static Properties toProperties(IndexDataSyncStatusData statusData) {
        Properties properties = new Properties();
        setValueIntoProperties("syncUID", statusData.getSyncUID(), properties);
        setValueIntoProperties("savePath", statusData.getSavePath(), properties);
        setValueIntoProperties("rotateFlagAfterSync", statusData.getRotateFlagAfterSync(), properties);
        setValueIntoProperties("fromStudentId", statusData.getFromStudentId(), properties);
        setValueIntoProperties("toStudentId", statusData.getToStudentId(), properties);
        setValueIntoProperties("finishStudentId", statusData.getFinishStudentId(), properties);
        setValueIntoProperties("syncCount", statusData.getSyncCount(), properties);
        setValueIntoProperties("syncType", statusData.getSyncType(), properties);
        setValueIntoProperties("finished", statusData.getFinished(), properties);
        setValueIntoProperties("success", statusData.getSuccess(), properties);
        setValueIntoProperties("beginTime", statusData.getBeginTime(), properties);
        setValueIntoProperties("finishTime", statusData.getFinishTime(), properties);
        return properties;
    }

    private static boolean validate(IndexDataSyncStatusData statusData) {
        if(StringUtils.isBlank(statusData.getSyncUID())) {
            return false;
        }
        if(StringUtils.isBlank(statusData.getSyncType())) {
            return false;
        }
        if(StringUtils.isBlank(statusData.getSavePath())) {
            return false;
        }
        if(StringUtils.isBlank(statusData.getRotateFlagAfterSync())) {
            return false;
        }
        if(statusData.getFromStudentId() == null || statusData.getToStudentId() == null) {
            return false;
        }
        return true;
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
        if (value == null) {
            return;
        }
        if (value instanceof Date) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
            properties.setProperty(key, simpleDateFormat.format(value));
        } else {
            properties.setProperty(key, value.toString());
        }
    }

    public static String generateSyncUID() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_UID_FORMAT);
        return simpleDateFormat.format(new Date()) + "-" + UUID.fastUUID().toString(true);
    }
}
