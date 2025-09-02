package org.vf.sbCriteriaPager.exception;

import org.vf.sbCriteriaPager.common.QueryStep;

public class InvalidArgumentException extends Exception {
    private QueryStep step;
    private String entitySearched;

    public InvalidArgumentException(QueryStep step, String entitySearched, String message) {
        super("Error during step: " + step.toString() + "for class: " + entitySearched + "message: " + message);

    }
}
