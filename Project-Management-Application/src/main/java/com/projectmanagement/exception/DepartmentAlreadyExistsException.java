package com.projectmanagement.exception;

public class DepartmentAlreadyExistsException extends Exception {
    public DepartmentAlreadyExistsException(String message) {
        super(message);
    }

    public DepartmentAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}