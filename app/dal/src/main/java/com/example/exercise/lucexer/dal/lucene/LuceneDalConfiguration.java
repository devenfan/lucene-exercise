package com.example.exercise.lucexer.dal.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * LuceneDalConfiguration
 *
 * @author Deven
 * @version : LuceneDalConfiguration, v 0.1 2020-03-11 21:42 Deven Exp$
 */
@Configuration
@ComponentScan("com.example.exercise.lucexer.dal.lucene")
public class LuceneDalConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LuceneDalConfiguration.class);

    @Value("${lucene.index.dir}")
    private String luceneIndexDir = "c:/temp/lucene7index/exercise";

    public String getIndexDir() {
        return luceneIndexDir;
    }

    public Path getIndexPath() {
        Path path = Paths.get(luceneIndexDir);
        File file = path.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    @Bean
    public Analyzer analyzer() {
        return new SmartChineseAnalyzer();
    }

    @Bean
    public Directory indexDirectory() throws IOException {
        Path path = getIndexPath();
        return FSDirectory.open(path);
    }

    @Bean
    public IndexWriter indexWriter(Directory indexDirectory, Analyzer analyzer) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        return new IndexWriter(indexDirectory, indexWriterConfig);
    }

    @Bean
    public SearcherManager searcherManager(Directory indexDirectory, IndexWriter indexWriter) throws IOException {
        SearcherManager searcherManager = new SearcherManager(indexWriter, false, false, new SearcherFactory());
        ControlledRealTimeReopenThread cRTReopenThead = new ControlledRealTimeReopenThread(indexWriter, searcherManager, 30.0, 10.0);
        cRTReopenThead.setDaemon(true);
        cRTReopenThead.setName("LuceneIndex动态加载线程");
        cRTReopenThead.start();
        logger.info(">>>>>>>>>> {} 已启动， 监控目录： {}", cRTReopenThead.getName(), indexDirectory);
        return searcherManager;
    }

}
