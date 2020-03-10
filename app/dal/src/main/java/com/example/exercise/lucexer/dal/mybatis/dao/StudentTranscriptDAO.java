package com.example.exercise.lucexer.dal.mybatis.dao;

import java.util.List;

import com.example.exercise.lucexer.dal.mybatis.domain.StudentTranscriptDO;

public interface StudentTranscriptDAO {

    List<StudentTranscriptDO> selectByStudentRange(Long studentIdFrom, Long studentIdTo);
}
