package com.digitalfix.workorders.exception;

public class WorkOrderAccessDeniedException extends RuntimeException {
    public WorkOrderAccessDeniedException(String message) {
        super(message);
    }
}
