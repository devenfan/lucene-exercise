package com.example.exercise.lucexer.dal.mybatis.domain;

/**
 * StudentTranscriptDO
 *
 * @author Deven
 * @version : StudentTranscriptDO, v 0.1 2020-03-10 22:59 Deven Exp$
 */
public class StudentTranscriptDO {

    public static final String GROUP_CONCAT_SEPARATOR = ",";

    private Long                studentId;

    private String              name;

    private Integer             age;

    private String              address;

    private Integer             sex;

    private Integer             grade;

    private String              courseNames;

    private String              courseScores;

    private String              scoreIds;

    private String              scoreTimestamps;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
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

    public String getScoreIds() {
        return scoreIds;
    }

    public void setScoreIds(String scoreIds) {
        this.scoreIds = scoreIds;
    }

    public String getScoreTimestamps() {
        return scoreTimestamps;
    }

    public void setScoreTimestamps(String scoreTimestamps) {
        this.scoreTimestamps = scoreTimestamps;
    }

}
