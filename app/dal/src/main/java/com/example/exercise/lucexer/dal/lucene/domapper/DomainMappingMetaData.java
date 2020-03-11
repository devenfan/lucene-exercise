package com.example.exercise.lucexer.dal.lucene.domapper;

import com.example.exercise.lucexer.dal.lucene.utils.LuceneDocAnno;
import com.example.exercise.lucexer.dal.lucene.utils.LuceneFieldAnno;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * DomainMappingMetaData
 *
 * @author Deven
 * @version : DomainMappingMetaData, v 0.1 2020-03-11 11:36 Deven Exp$
 */
class DomainMappingMetaData<T> {

    private Class<T>                    beanClass;
    private LuceneDocAnno               indexDocMeta;
    private Map<Field, LuceneFieldAnno> beanField2IndexFieldMap;

    public Class<T> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    public LuceneDocAnno getIndexDocMeta() {
        return indexDocMeta;
    }

    public void setIndexDocMeta(LuceneDocAnno indexDocMeta) {
        this.indexDocMeta = indexDocMeta;
    }

    public Map<Field, LuceneFieldAnno> getBeanField2IndexFieldMap() {
        return beanField2IndexFieldMap;
    }

    public void setBeanField2IndexFieldMap(Map<Field, LuceneFieldAnno> beanField2IndexFieldMap) {
        this.beanField2IndexFieldMap = beanField2IndexFieldMap;
    }

}
