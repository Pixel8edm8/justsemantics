package com.theethancorporation.util;

public class MalformedWikipediaURLException extends Exception {
    public MalformedWikipediaURLException(String message) {
        super(message);
    }

    public MalformedWikipediaURLException(String message, Throwable cause) {
        super(message, cause);
    }
}
