package com.example.exercise.lucexer.common;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * IndexDataSyncStatusFileMonitor
 *
 * @author Deven
 * @version : IndexDataSyncStatusFileMonitor, v 0.1 2020-03-22 12:00 Deven Exp$
 */
public class IndexDataSyncStatusFileMonitor {

    private static final Logger                     logger              = LoggerFactory.getLogger(IndexDataSyncStatusFileMonitor.class);

    /**
     * 监测器线程名称
     */
    private static final String                     MONITOR_THREAD_NAME = "IndexDataSyncStatusData FILE MONITOR Daemon";

    /**
     * 监测器线程Daemon标记
     */
    private static final boolean                    DAEMON              = false;

    /**
     * 文件变化监测器
     */
    private FileAlterationMonitor                   monitor;

    /**
     * 文件变化观察者
     */
    private FileAlterationObserver                  observer;

    /**
     * 状态变化监听器（外部）
     */
    private List<IndexDataSyncStatusChangeListener> listeners           = new ArrayList<>();

    /**
     * 状态文件目录
     */
    private String                                  syncStatusFileDir;

    /**
     * 状态文件名（不含目录）
     */
    private String                                  syncStatusFileName;

    /**
     * 构造函数
     * @param syncStatusFileDir   状态文件目录
     * @param syncStatusFileName   状态文件名（不含目录）
     * @param intervalMillis    监测时间间隔（毫秒）
     */
    public IndexDataSyncStatusFileMonitor(String syncStatusFileDir, String syncStatusFileName, int intervalMillis) {
        this.syncStatusFileDir = syncStatusFileDir;
        this.syncStatusFileName = syncStatusFileName;
        this.observer = new FileAlterationObserver(IndexDataSyncStatusFileMonitor.this.syncStatusFileDir, new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().equals(IndexDataSyncStatusFileMonitor.this.syncStatusFileName);
            }
        });
        this.observer.addListener(new FileAlterationListener() {
            @Override
            public void onStart(FileAlterationObserver fileAlterationObserver) {
                logger.debug("fileAlterationObserver started: {}", fileAlterationObserver);
            }

            @Override
            public void onDirectoryCreate(File file) {
                logger.warn("onDirectoryCreate: {}", file);
            }

            @Override
            public void onDirectoryChange(File file) {
                logger.warn("onDirectoryChange: {}", file);
            }

            @Override
            public void onDirectoryDelete(File file) {
                logger.warn("onDirectoryDelete: {}", file);
            }

            @Override
            public void onFileCreate(File file) {
                logger.warn("onFileCreate: {}", file);
                IndexDataSyncStatusData statusData = IndexDataSyncStatusData.loadFromFile(file);
                invokeListeners(statusData);
            }

            @Override
            public void onFileChange(File file) {
                logger.warn("onFileChange: {}", file);
                IndexDataSyncStatusData statusData = IndexDataSyncStatusData.loadFromFile(file);
                invokeListeners(statusData);
            }

            @Override
            public void onFileDelete(File file) {
                logger.warn("onFileDelete: {}", file);
            }

            @Override
            public void onStop(FileAlterationObserver fileAlterationObserver) {
                logger.debug("fileAlterationObserver stopped: {}", fileAlterationObserver);
            }
        });
        this.monitor = new FileAlterationMonitor(intervalMillis, new FileAlterationObserver[] { observer });
        this.monitor.setThreadFactory(new BasicThreadFactory.Builder().namingPattern(MONITOR_THREAD_NAME).daemon(DAEMON).build());
    }

    /**
     * 添加监听器
     */
    public void addListener(IndexDataSyncStatusChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 删除监听器
     */
    public void removeListener(IndexDataSyncStatusChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * 获取注册的所有监听器
     */
    public Iterator<IndexDataSyncStatusChangeListener> getListeners() {
        return listeners.iterator();
    }

    /**
     * 启动监测器
     */
    public void start() {
        try {
            monitor.start();
            logger.warn("{} on {} Started!", getClass().getSimpleName(), getSyncStatusFilePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 停止监测器
     */
    public void stop() {
        try {
            monitor.stop();
            logger.warn("{} on {} Stopped!", getClass().getSimpleName(), getSyncStatusFilePath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取监测时间间隔
     */
    public long getInterval() {
        return monitor.getInterval();
    }

    /**
     * 调用监听器
     * @param statusData
     */
    private void invokeListeners(IndexDataSyncStatusData statusData) {
        for (IndexDataSyncStatusChangeListener listener : listeners) {
            try {
                listener.onStatusChanged(statusData);
            } catch (Exception ex) {
                logger.error("Invoke listener error", ex);
            }
        }
    }

    private String getSyncStatusFilePath() {
        return syncStatusFileDir + "/" + syncStatusFileName;
    }

}
