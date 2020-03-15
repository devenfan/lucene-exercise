package com.example.exercise.lucexer.common;

import java.util.ArrayList;
import java.util.List;

/**
 * BeanMapper
 *
 * @author Deven
 * @version : BeanMapper, v 0.1 2020-03-15 11:59 Deven Exp$
 */
public interface BeanMapper<A, B> {

    B mapA2B(A a);

    A mapB2A(B b);

    default List<B> mapAS2BS(List<A> aList) {
        if(aList == null) {
            return null;
        }
        List<B> bList = new ArrayList<>(aList.size());
        for (A a : aList) {
            bList.add(mapA2B(a));
        }
        return bList;
    }

    default List<A> mapBS2AS(List<B> bList) {
        if(bList == null) {
            return null;
        }
        List<A> aList = new ArrayList<>(bList.size());
        for (B b : bList) {
            aList.add(mapB2A(b));
        }
        return aList;
    }
}
