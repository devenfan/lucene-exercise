package com.example.exercise.lucexer.common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * IndexDataSyncStatusDataTest
 *
 * @author Deven
 * @version : IndexDataSyncStatusDataTest, v 0.1 2020-03-22 15:33 Deven Exp$
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class IndexDataSyncStatusDataTest {

    @Test
    public void saveToFile() {
        IndexDataSyncStatusData statusData = new IndexDataSyncStatusData();
        IndexDataSyncStatusData.writeToFile(statusData, "c:/temp/lucene7index/exercise/status/indexSyncStatus.properties.test");
    }
}
