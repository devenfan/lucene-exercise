package com.example.exercise.lucexer.dal.lucene;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.example.exercise.lucexer.common.IndexDataSyncStatusChangeListener;
import com.example.exercise.lucexer.common.IndexDataSyncStatusFileMonitor;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.example.exercise.lucexer.common.IndexDataSyncConstants;
import com.example.exercise.lucexer.common.IndexDataSyncStatusData;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * LuceneDalDynamicSearcher
 *
 * @author Deven
 * @version : LuceneDalDynamicSearcher, v 0.1 2020-03-22 13:26 Deven Exp$
 */
@Component
public class LuceneDalDynamicSearcher implements InitializingBean, DisposableBean {

    private static final Logger            logger             = LoggerFactory.getLogger(LuceneDalDynamicSearcher.class);

    /**
     * 当前的索引主目录
     */
    @Value("${lucene.index.dir.main}")
    private String                         indexMainDir       = "c:/temp/lucene7index/exercise/index";

    /**
     * 当前的索引的目录轮换标识
     */
    @Value("${lucene.index.dir.rotateFlag}")
    private String                         currentRotateFlag  = "A";

    /**
     * 状态文件所在目录
     */
    @Value("${lucene.syncStatus.fileDir}")
    private String                         syncStatusFileDir  = "c:/temp/lucene7index/exercise/status";

    /**
     * 状态文件名
     */
    @Value("${lucene.syncStatus.fileName}")
    private String                         syncStatusFileName = "indexSyncStatus.properties";

    @Resource
    private Analyzer                       analyzer;

    private FSDirectory                    indexDirectory;

    private IndexWriter                    indexWriter;

    private SearcherManager                searcherManager;

    private ControlledRealTimeReopenThread reopenThread;

    private IndexDataSyncStatusData        currentIndexDataSyncStatusData;

    private IndexDataSyncStatusFileMonitor indexDataSyncStatusFileMonitor;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.currentIndexDataSyncStatusData = IndexDataSyncStatusData.loadFromFile(getSyncStatusFilePath());
        refresh(this.currentIndexDataSyncStatusData);
        indexDataSyncStatusFileMonitor = new IndexDataSyncStatusFileMonitor(this.syncStatusFileDir, this.syncStatusFileName, 60 * 1000);
        indexDataSyncStatusFileMonitor.addListener(new IndexDataSyncStatusChangeListener() {
            @Override
            public void onStatusChanged(IndexDataSyncStatusData statusData) {
                LuceneDalDynamicSearcher.this.refresh(statusData);
            }
        });
        indexDataSyncStatusFileMonitor.start();
    }

    @Override
    public void destroy() throws Exception {
        if(indexDataSyncStatusFileMonitor != null) {
            indexDataSyncStatusFileMonitor.stop();
        }
        if (reopenThread != null) {
            reopenThread.close();
        }
        closeResource(searcherManager);
        closeResource(indexWriter);
        closeResource(indexDirectory);
    }

    public Directory getIndexDirectory() {
        return indexDirectory;
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public SearcherManager getSearcherManager() {
        return this.searcherManager;
    }

    synchronized void refresh(IndexDataSyncStatusData statusData) {

        logger.info(">>>>>>>>>> Refresh DynamicSearcher: {}", JSON.toJSONString(statusData));

        //仅第一次加载，或全量同步后，才需要进行刷新
        boolean needRefresh = (statusData == null) || (IndexDataSyncConstants.SYNC_TYPE_FULL.equals(statusData.getSyncType()));
        if (!needRefresh) {
            return;
        }

        String uid = "DEFAULT";
        final String currentIndexPath = getCurrentIndexPath();
        String newIndexPath = currentIndexPath;
        if (statusData != null) {
            newIndexPath = statusData.getSavePath();
            uid = statusData.getSyncUID();
        }

        FSDirectory oldDirectory = this.indexDirectory;
        IndexWriter oldIndexWriter = this.indexWriter;
        SearcherManager oldSearcherManager = this.searcherManager;
        ControlledRealTimeReopenThread oldReopenThread = reopenThread;

        //打开新索引
        if(openResources(newIndexPath, uid)) {
            synchronized (this) {
                if (statusData != null) {
                    this.currentIndexDataSyncStatusData = statusData;
                    this.currentRotateFlag = statusData.getRotateFlagAfterSync();
                }
            }
            logger.warn(">>>>>>>>>> Refresh DynamicSearcher successfully on newIndexPath: {}", newIndexPath);
            closeResources(oldDirectory, oldIndexWriter, oldSearcherManager, oldReopenThread);
            return;
        }

        logger.error(">>>>>>>>>> Refresh DynamicSearcher failed on newIndexPath: {}", newIndexPath);
    }

    private boolean openResources(String indexPath, String uid) {
        FSDirectory newDirectory = null;
        IndexWriter newIndexWriter = null;
        SearcherManager newSearcherManager = null;
        ControlledRealTimeReopenThread newReopenThread = null;

        try {
            newDirectory = FSDirectory.open(getPath(indexPath));
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            newIndexWriter = new IndexWriter(newDirectory, indexWriterConfig);
            newSearcherManager = new SearcherManager(newIndexWriter, false, false, new SearcherFactory());
        } catch (Exception ex) {
            closeResource(newSearcherManager);
            closeResource(newIndexWriter);
            closeResource(newDirectory);
            logger.error("Refresh DynamicSearcher error: {}", ex.getMessage(), ex);
            return false;
        }

        newReopenThread = newReopenThread(uid, newDirectory, newIndexWriter, newSearcherManager);

        synchronized (this) {
            this.indexDirectory = newDirectory;
            this.indexWriter = newIndexWriter;
            this.searcherManager = newSearcherManager;
            this.reopenThread = newReopenThread;
        }
        return true;
    }

    private void closeResources(FSDirectory indexDirectory, IndexWriter indexWriter, SearcherManager searcherManager, ControlledRealTimeReopenThread reopenThread) {
        {
            if (reopenThread != null) {
                reopenThread.close();
                logger.info(">>>>>>>>>> {} 已关闭， 监控目录： {}", reopenThread.getName(), indexDirectory);
            }
            closeResource(searcherManager);
            closeResource(indexWriter);
            closeResource(indexDirectory);
        }
    }


    private ControlledRealTimeReopenThread newReopenThread(String uid, Directory indexDirectory, IndexWriter indexWriter, SearcherManager searcherManager) {
        ControlledRealTimeReopenThread cRTReopenThead = new ControlledRealTimeReopenThread(indexWriter, searcherManager, 30.0, 10.0);
        cRTReopenThead.setDaemon(true);
        cRTReopenThead.setName("LuceneIndex动态加载线程-" + uid);
        cRTReopenThead.start();
        logger.info(">>>>>>>>>> {} 已启动， 监控目录： {}", cRTReopenThead.getName(), indexDirectory);
        return cRTReopenThead;
    }

    private String getCurrentIndexPath() {
        return indexMainDir + "/" + currentRotateFlag;
    }

    public String getSyncStatusFilePath() {
        return syncStatusFileDir + "/" + syncStatusFileName;
    }

    private Path getPath(String pathStr) {
        Path path = Paths.get(pathStr);
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    private void closeResource(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
