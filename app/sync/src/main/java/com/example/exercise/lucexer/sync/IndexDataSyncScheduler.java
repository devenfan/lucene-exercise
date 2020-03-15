package com.example.exercise.lucexer.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * IndexDataSyncScheduler
 *
 * @author Deven
 * @version : IndexDataSyncScheduler, v 0.1 2020-03-14 16:05 Deven Exp$
 */
@Component
public class IndexDataSyncScheduler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final static long ONE_DAY    = 24 * 60 * 60 * 1000;
    public final static long ONE_HOUR   = 60 * 60 * 1000;
    public final static long ONE_MINUTE = 60 * 1000;
    public final static long ONE_SECOND = 1000;

    @Resource
    private IndexDataSyncService indexDataSyncService;

    @Scheduled(cron = "0 0/1 * * * ? ")
    public void incrementalDataSync() {
        logger.info("incrementalDataSync begin......");
        indexDataSyncService.syncIncremental();
        logger.info("incrementalDataSync finished......");
    }

    @Scheduled(cron = "0 0/10 * * * ? ")
    public void fullDataSync() {
        logger.info("fullDataSync begin......");
        indexDataSyncService.syncAll();
        logger.info("fullDataSync finished......");
    }


}
