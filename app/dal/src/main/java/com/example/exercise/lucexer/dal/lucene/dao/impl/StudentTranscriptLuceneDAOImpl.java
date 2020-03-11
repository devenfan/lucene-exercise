package com.example.exercise.lucexer.dal.lucene.dao.impl;

import javax.annotation.Resource;

import org.apache.lucene.search.SearcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * StudentTranscriptLuceneDAOImpl
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAOImpl, v 0.1 2020-03-11 00:20 Deven Exp$
 */
@Repository
public class StudentTranscriptLuceneDAOImpl {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SearcherManager searcherManager;

}
