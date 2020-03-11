package com.example.exercise.lucexer.dal.lucene.domapper;

import com.example.exercise.lucexer.dal.lucene.utils.LuceneDocAnno;
import com.example.exercise.lucexer.dal.lucene.utils.LuceneFieldAnno;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractLuceneDomainMapper
 *
 * @author Deven
 * @version : AbstractLuceneDomainMapper, v 0.1 2020-03-11 11:31 Deven Exp$
 */
public abstract class AbstractLuceneDomainMapper<T> implements LuceneDomainMapper<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private DomainMappingMetaData<T> mappingMetaData;

    public AbstractLuceneDomainMapper() {
        Class<T> clazz = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        init(clazz);
    }

    public AbstractLuceneDomainMapper(Class<T> clazz) {
        init(clazz);
    }

    private void init(Class<T> clazz) {
        DomainMappingMetaData mappingMetaData = new DomainMappingMetaData();
        mappingMetaData.setBeanClass(clazz);
        mappingMetaData.setIndexDocMeta((LuceneDocAnno) clazz.getAnnotation(LuceneDocAnno.class));
        java.lang.reflect.Field beanFields[] = clazz.getDeclaredFields();
        Map<Field, LuceneFieldAnno> beanField2IndexFieldMap = new HashMap<>(beanFields.length);
        for (Field beanField : beanFields) {
            LuceneFieldAnno indexFieldMeta = beanField.getAnnotation(LuceneFieldAnno.class);
            if(indexFieldMeta != null) {
                beanField2IndexFieldMap.put(beanField, indexFieldMeta);
            }
        }
        mappingMetaData.setBeanField2IndexFieldMap(beanField2IndexFieldMap);
        this.mappingMetaData = mappingMetaData;
    }

    @Override
    public T doc2bean(Document document) {
        T bean = null;
        try {
            bean = mappingMetaData.getBeanClass().newInstance();
            for (Map.Entry<Field, LuceneFieldAnno> fieldFieldEntry : mappingMetaData.getBeanField2IndexFieldMap().entrySet()) {
                Field beanField = fieldFieldEntry.getKey();
                LuceneFieldAnno indexFieldInfo = fieldFieldEntry.getValue();
                beanField.setAccessible(true);
                setBeanField(bean, beanField, document, indexFieldInfo);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("doc2bean error", e);
            throw new IllegalStateException("doc2bean error", e);
        }
        return bean;
    }

    @Override
    public Document bean2doc(T bean) {
        Document document = new Document();
        try {
            for (Map.Entry<Field, LuceneFieldAnno> fieldFieldEntry : mappingMetaData.getBeanField2IndexFieldMap().entrySet()) {
                Field beanField = fieldFieldEntry.getKey();
                LuceneFieldAnno indexFieldInfo = fieldFieldEntry.getValue();
                setIndexField(bean, beanField, document, indexFieldInfo);
            }
        } catch (IllegalAccessException e) {
            logger.error("bean2doc error", e);
            throw new IllegalStateException("bean2doc error", e);
        }
        //logger.info("document: {}", document.toString());
        return document;
    }


    private void setBeanField(T beanObj, Field beanField, Document document, LuceneFieldAnno indexFieldInfo) throws IllegalAccessException {
        final String stringValue = document.get(indexFieldInfo.fieldName());
        Object valueWithType = null;
        if(stringValue != null) {
            if(indexFieldInfo.fieldType() == IntPoint.class) {
                valueWithType = Integer.valueOf(stringValue);
            } else if(indexFieldInfo.fieldType() == LongPoint.class) {
                valueWithType = Long.valueOf(stringValue);
            } else if(indexFieldInfo.fieldType() == DoublePoint.class) {
                valueWithType = Double.valueOf(stringValue);
            } else if(indexFieldInfo.fieldType() == StringField.class || indexFieldInfo.fieldType() == TextField.class) {
                valueWithType = stringValue;
            } else {
                throw new IllegalStateException("Lucene Filed Type not supported: " + indexFieldInfo.fieldType());
            }
            beanField.setAccessible(true);
            beanField.set(beanObj, valueWithType);
        }
    }

    private void setIndexField(T beanObj, Field beanField, Document document, LuceneFieldAnno indexFieldInfo) throws IllegalAccessException {
        beanField.setAccessible(true);
        final Object valueWithType = beanField.get(beanObj);
        org.apache.lucene.document.Field indexField = null;
        org.apache.lucene.document.Field preSortField = null;
        if(valueWithType != null) {
            if(indexFieldInfo.fieldType() == IntPoint.class) {
                indexField = new IntPoint(indexFieldInfo.fieldName(), (Integer)valueWithType);
                preSortField = indexFieldInfo.preSort() ? new NumericDocValuesField(indexFieldInfo.fieldName(), Long.valueOf(valueWithType.toString())) : null;
            } else if(indexFieldInfo.fieldType() == LongPoint.class) {
                indexField = new LongPoint(indexFieldInfo.fieldName(), (Long)valueWithType);
                preSortField = indexFieldInfo.preSort() ? new NumericDocValuesField(indexFieldInfo.fieldName(), Long.valueOf(valueWithType.toString())) : null;
            } else if(indexFieldInfo.fieldType() == DoublePoint.class) {
                indexField = new DoublePoint(indexFieldInfo.fieldName(), (Double)valueWithType);
            } else if(indexFieldInfo.fieldType() == StringField.class) {
                indexField = new StringField(indexFieldInfo.fieldName(), valueWithType.toString(), org.apache.lucene.document.Field.Store.YES);
                preSortField = indexFieldInfo.preSort() ? new SortedDocValuesField(indexFieldInfo.fieldName(), new BytesRef(valueWithType.toString())) : null;
            } else if(indexFieldInfo.fieldType() == TextField.class) {
                indexField = new TextField(indexFieldInfo.fieldName(), valueWithType.toString(), org.apache.lucene.document.Field.Store.YES);
            } else {
                throw new IllegalStateException("Lucene Filed Type not supported: " + indexFieldInfo.fieldType());
            }
            document.add(indexField);
            if(preSortField != null) {
                document.add(preSortField);
            }
        }

    }

}
