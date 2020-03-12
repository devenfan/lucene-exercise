package com.example.exercise.lucexer.dal.lucene;

/**
 * LuceneDalException
 *
 * @author Deven
 * @version : LuceneDalException, v 0.1 2020-03-13 00:57 Deven Exp$
 */
public class LuceneDalException extends RuntimeException {

    public LuceneDalException() {
    }

    public LuceneDalException(String message) {
        super(message);
    }

    public LuceneDalException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuceneDalException(Throwable cause) {
        super(cause);
    }

    public LuceneDalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
