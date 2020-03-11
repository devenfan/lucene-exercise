package com.example.exercise.lucexer.dal.lucene.domain;

import com.example.exercise.lucexer.dal.lucene.utils.LuceneDocAnno;
import com.example.exercise.lucexer.dal.lucene.utils.LuceneFieldAnno;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * StudentTranscriptLuceneDO
 *
 * @author Deven
 * @version : StudentTranscriptLuceneDO, v 0.1 2020-03-11 00:09 Deven Exp$
 */
@LuceneDocAnno(indexName = "StudentTranscript")
public class StudentTranscriptLuceneDO {

    /**
     * 学生id，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "studentId", fieldType = LongPoint.class)
    private Long studentId;

    /**
     * 姓名，作为文本，分词且索引
     */
    @LuceneFieldAnno(fieldName = "name", fieldType = TextField.class)
    private String name;

    /**
     * 姓氏，作为字符串，不分词只索引
     */
    @LuceneFieldAnno(fieldName = "familyName", fieldType = StringField.class)
    private String familyName;

    /**
     * 年龄，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "age", fieldType = IntPoint.class)
    private Integer age;

    /**
     * 地址，作为文本，分词且索引
     */
    @LuceneFieldAnno(fieldName = "address", fieldType = TextField.class)
    private String address;

    /**
     * 省, 作为字符串，不分词只索引
     */
    @LuceneFieldAnno(fieldName = "province", fieldType = StringField.class)
    private String province;

    /**
     * 城市, 作为字符串，不分词只索引
     */
    @LuceneFieldAnno(fieldName = "city", fieldType = StringField.class)
    private String city;

    /**
     * 城区, 作为字符串，不分词只索引
     */
    @LuceneFieldAnno(fieldName = "cityArea", fieldType = StringField.class)
    private String cityArea;

    /**
     * 小区, 作为文本，分词且索引
     */
    @LuceneFieldAnno(fieldName = "village", fieldType = TextField.class)
    private String village;

    /**
     * 是否住别墅（0和1）, 作为数值型，分词且索引，并且预索引
     */
    @LuceneFieldAnno(fieldName = "villaHouse", fieldType = IntPoint.class, preSort = true)
    private Integer villaHouse;

    /**
     * 门牌号, 作为文本，分词且索引
     */
    @LuceneFieldAnno(fieldName = "houseNumber", fieldType = TextField.class)
    private String houseNumber;

    /**
     * 性别，作为字符串，不分词只索引
     */
    @LuceneFieldAnno(fieldName = "sex", fieldType = StringField.class)
    private String sex;

    /**
     * 年级，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "grade", fieldType = IntPoint.class)
    private Integer grade;

    /**
     * 语文分数，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreYuwen", fieldType = DoublePoint.class)
    private Double scoreYuwen;

    /**
     * 数学分数，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreShuxue", fieldType = DoublePoint.class)
    private Double scoreShuxue;

    /**
     * 化学分数，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreHuaxue", fieldType = DoublePoint.class)
    private Double scoreHuaxue;

    /**
     * 个人最高分，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreHigh", fieldType = DoublePoint.class)
    private Double scoreHigh;

    /**
     * 个人最低分，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreLow", fieldType = DoublePoint.class)
    private Double scoreLow;

    /**
     * 个人平均分，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreAvg", fieldType = DoublePoint.class)
    private Double scoreAvg;

    /**
     * 个人总分，数值型，分词且索引
     */
    @LuceneFieldAnno(fieldName = "scoreSum", fieldType = DoublePoint.class)
    private Double scoreSum;

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

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityArea() {
        return cityArea;
    }

    public void setCityArea(String cityArea) {
        this.cityArea = cityArea;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public Integer getVillaHouse() {
        return villaHouse;
    }

    public void setVillaHouse(Integer villaHouse) {
        this.villaHouse = villaHouse;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getScoreYuwen() {
        return scoreYuwen;
    }

    public void setScoreYuwen(Double scoreYuwen) {
        this.scoreYuwen = scoreYuwen;
    }

    public Double getScoreShuxue() {
        return scoreShuxue;
    }

    public void setScoreShuxue(Double scoreShuxue) {
        this.scoreShuxue = scoreShuxue;
    }

    public Double getScoreHuaxue() {
        return scoreHuaxue;
    }

    public void setScoreHuaxue(Double scoreHuaxue) {
        this.scoreHuaxue = scoreHuaxue;
    }

    public Double getScoreHigh() {
        return scoreHigh;
    }

    public void setScoreHigh(Double scoreHigh) {
        this.scoreHigh = scoreHigh;
    }

    public Double getScoreLow() {
        return scoreLow;
    }

    public void setScoreLow(Double scoreLow) {
        this.scoreLow = scoreLow;
    }

    public Double getScoreAvg() {
        return scoreAvg;
    }

    public void setScoreAvg(Double scoreAvg) {
        this.scoreAvg = scoreAvg;
    }

    public Double getScoreSum() {
        return scoreSum;
    }

    public void setScoreSum(Double scoreSum) {
        this.scoreSum = scoreSum;
    }

}
