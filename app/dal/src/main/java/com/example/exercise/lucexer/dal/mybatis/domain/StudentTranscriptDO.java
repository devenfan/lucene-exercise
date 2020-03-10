package com.example.exercise.lucexer.dal.mybatis.domain;

import java.util.List;

/**
 * StudentTranscriptDO
 *
 * @author Deven
 * @version : StudentTranscriptDO, v 0.1 2020-03-10 22:59 Deven Exp$
 */
public class StudentTranscriptDO {

    private static final String GROUP_CONCAT_SEPARATOR = ",";

    private Long                studentId;

    private String              courseNames;

    private String              courseScores;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getCourseNames() {
        return courseNames;
    }

    public void setCourseNames(String courseNames) {
        this.courseNames = courseNames;
    }

    public String getCourseScores() {
        return courseScores;
    }

    public void setCourseScores(String courseScores) {
        this.courseScores = courseScores;
    }

}
