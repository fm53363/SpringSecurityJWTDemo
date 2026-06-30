package com.fer.hr.springsecurityjwtdemo.exception;

public class PasswordsDontMatchException extends RuntimeException {

    public PasswordsDontMatchException() {
        super("Passwords do not match");
    }
}
