package com.example.exercise.lucexer.demo.biz;

import java.util.List;

import javax.annotation.Resource;

import com.example.exercise.lucexer.dal.mybatis.dao.CourseDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentDAO;
import com.example.exercise.lucexer.dal.mybatis.dao.StudentTranscriptDAO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentMaxMinIdInfoDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoBizService {

    @Resource
    private StudentDAO           studentDAO;

    @Resource
    private CourseDAO            courseDAO;

    @Resource
    private StudentTranscriptDAO studentTranscriptDAO;

    @Transactional
    public StudentDO findStudentById(Long id) {
        return studentDAO.selectByPrimaryKey(id);
    }

    @Transactional
    public List<StudentDO> findStudentsByName(String name, boolean exactly) {
        if (exactly) {
            return studentDAO.selectByNameExactly(name);
        } else {
            return studentDAO.selectByNameLikely(name);
        }
    }

    public List<StudentDO> findStudentsByRange(Long fromId, Long toId) {
        return studentDAO.selectStudentsByIdRange(fromId, toId);
    }

    public StudentMaxMinIdInfoDO queryStudentMaxMinIdInfo() {
        return studentDAO.queryMaxMinIdInfo();
    }

    public List<String> listAllCourses() {
        return courseDAO.listCourses();
    }

    public List<StudentTranscriptDO> queryStudentTranscripts(Long fromStudentId, Long toStudentId) {
        return studentTranscriptDAO.selectByStudentRange(fromStudentId, toStudentId);
    };
}
