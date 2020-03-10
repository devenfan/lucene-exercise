package com.example.exercise.lucexer.dal.mybatis.domain;

/**
 * StudentMaxMinIdInfoDO
 *
 * @author Deven
 * @version : StudentMaxMinIdInfoDO, v 0.1 2020-03-10 23:12 Deven Exp$
 */
public class StudentMaxMinIdInfoDO {

    private Long minId;

    private Long maxId;

    private Long totalCount;

    public Long getMinId() {
        return minId;
    }

    public void setMinId(Long minId) {
        this.minId = minId;
    }

    public Long getMaxId() {
        return maxId;
    }

    public void setMaxId(Long maxId) {
        this.maxId = maxId;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

}
