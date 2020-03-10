package com.example.exercise.lucexer.dal.mybatis.dao;

import com.example.exercise.lucexer.dal.mybatis.domain.StudentDO;
import com.example.exercise.lucexer.dal.mybatis.domain.StudentMaxMinIdInfoDO;

import java.util.List;


public interface StudentDAO {

    int deleteByPrimaryKey(Long id);

    int insert(StudentDO record);

    int insertSelective(StudentDO record);

    StudentDO selectByPrimaryKey(Long id);

    List<StudentDO> selectByNameExactly(String name);

    List<StudentDO> selectByNameLikely(String name);

    int updateByPrimaryKeySelective(StudentDO record);

    int updateByPrimaryKey(StudentDO record);

    List<StudentDO> selectStudentsByIdRange(Long fromId, Long toId);

    StudentMaxMinIdInfoDO queryMaxMinIdInfo();
}
