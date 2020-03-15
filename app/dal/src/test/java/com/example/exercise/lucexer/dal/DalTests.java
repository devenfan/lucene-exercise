package com.example.exercise.lucexer.dal;

import com.alibaba.fastjson.JSON;
import com.example.exercise.lucexer.dal.lucene.LuceneDalConfiguration;
import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.dal.mybatis.MybatisDalConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class DalTests {

    @Resource
    StudentTranscriptLuceneDAO studentTranscriptLuceneDAO;

    @Test
    public void contextLoads() {
    }

    protected void println(Object obj){
        System.out.println(JSON.toJSONString(obj));
    }

}
