package com.example.exercise.lucexer.dal.lucene.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.example.exercise.lucexer.dal.lucene.LuceneDalException;
import com.example.exercise.lucexer.dal.lucene.dao.StudentTranscriptLuceneDAO;
import com.example.exercise.lucexer.dal.lucene.domain.StudentTranscriptLuceneDO;
import com.example.exercise.lucexer.dal.lucene.domapper.StudentTranscriptLuceneDomainMapper;
import com.example.exercise.lucexer.dal.lucene.utils.LuceneIndexSearchUtils;

/**
 * StudentTranscriptLuceneDAOImpl
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDAOImpl, v 0.1 2020-03-11 00:20 Deven Exp$
 */
@Repository
public class StudentTranscriptLuceneDAOImpl extends AbstractLuceneDAO implements StudentTranscriptLuceneDAO {

    private final Logger                        logger               = LoggerFactory.getLogger(getClass());

    private static final int                    DEFAULT_RECORD_LIMIT = 1000;

    @Resource
    private StudentTranscriptLuceneDomainMapper studentTranscriptLuceneDomainMapper;

    @Override
    public List<StudentTranscriptLuceneDO> query(String operationName, Query query, Sort sort, int limit) {
        return doQueryDocs(operationName, query, sort, limit > 0 ? limit : DEFAULT_RECORD_LIMIT);
    }

    @Override
    public long count(Query filterQuery) {
        try {
            IndexSearcher indexSearcher = LuceneIndexSearchUtils.getIndexSearcherFromThreadLocal();
            return doSelectCount(indexSearcher, filterQuery);
        } catch (Exception e) {
            logger.error("count error: {}", e.getMessage());
            throw new LuceneDalException("count error", e);
        }
    }

    @Override
    public Map<String, Long> groupCountByField(String operationName, Query filterQuery, String groupByField, int maxGroups, int maxDocsPerGroup) {
        try {
            IndexSearcher indexSearcher = LuceneIndexSearchUtils.getIndexSearcherFromThreadLocal();
            //分组查询
            TopGroups<BytesRef> topGroups = doGroupBy(indexSearcher, filterQuery, groupByField, maxGroups, maxDocsPerGroup);
            if(topGroups == null) {
                return new HashMap<>(0);
            }
            Map<String, Long> result = new HashMap<>(topGroups.groups.length);
            GroupDocs<BytesRef>[] gds = topGroups.groups;
            for (GroupDocs<BytesRef> gd : gds) {
                result.put(gd.groupValue.utf8ToString(), gd.totalHits);
            }
            return result;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }
    }

    @Override
    public boolean update(StudentTranscriptLuceneDO luceneDO) {
        try {
            Document document = studentTranscriptLuceneDomainMapper.domain2Doc(luceneDO);
            IndexWriter indexWriter = dynamicSearcher.getIndexWriter();
            indexWriter.updateDocument(new Term("studentId", luceneDO.getStudentId().toString()), document);
            indexWriter.commit();
            return true;
        } catch (IOException e) {
            logger.error("update one error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(List<StudentTranscriptLuceneDO> luceneDOS) {
        try {
            IndexWriter indexWriter = dynamicSearcher.getIndexWriter();
            for (StudentTranscriptLuceneDO luceneDO : luceneDOS) {
                Document document = studentTranscriptLuceneDomainMapper.domain2Doc(luceneDO);
                indexWriter.updateDocument(new Term("studentId", luceneDO.getStudentId().toString()), document);
            }
            indexWriter.commit();
            return true;
        } catch (IOException e) {
            logger.error("update list error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(StudentTranscriptLuceneDO luceneDO) {
        try {
            IndexWriter indexWriter = dynamicSearcher.getIndexWriter();
            indexWriter.deleteDocuments(new Term("studentId", luceneDO.getStudentId().toString()));
            indexWriter.commit();
            return true;
        } catch (IOException e) {
            logger.error("delete one error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(List<StudentTranscriptLuceneDO> luceneDOS) {
        try {
            IndexWriter indexWriter = dynamicSearcher.getIndexWriter();
            for (StudentTranscriptLuceneDO luceneDO : luceneDOS) {
                indexWriter.deleteDocuments(new Term("studentId", luceneDO.getStudentId().toString()));
            }
            indexWriter.commit();
            return true;
        } catch (IOException e) {
            logger.error("delete list error: {}", e.getMessage());
            return false;
        }
    }

    // Private ----------------------------------------------------------------------------------------

    private List<StudentTranscriptLuceneDO> doQueryDocs(String operationName, Query query, Sort sort, int limit) {
        try {
            IndexSearcher indexSearcher = LuceneIndexSearchUtils.getIndexSearcherFromThreadLocal();
            if(sort == null) {
                sort = new Sort(new SortField(null, SortField.Type.SCORE));
            }
            TopDocs docs = doSelectDocs(indexSearcher, query, limit, sort);
            List<StudentTranscriptLuceneDO> resultList = new ArrayList<>(docs.scoreDocs == null ? 0 : docs.scoreDocs.length);
            if (docs.scoreDocs != null) {
                for (ScoreDoc scoreDoc : docs.scoreDocs) {
                    Document document = indexSearcher.doc(scoreDoc.doc);
                    StudentTranscriptLuceneDO luceneDO = studentTranscriptLuceneDomainMapper.doc2domain(document);
                    if (luceneDO != null) {
                        resultList.add(luceneDO);
                    }
                }
            }
            return resultList;
        } catch (Exception e) {
            logger.error("{} error: {}", operationName, e.getMessage());
            throw new LuceneDalException(operationName + " error", e);
        }
    }

}
