package com.fer.hr.springsecurityjwtdemo.exception;

public class UsernameTakenException extends RuntimeException {

    public UsernameTakenException() {
        super("Username already exists");
    }
}
