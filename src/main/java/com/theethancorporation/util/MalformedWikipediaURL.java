package com.theethancorporation.util;

public class MalformedWikipediaURL extends Exception {
    public MalformedWikipediaURL(String message) {
        super(message);
    }

    public MalformedWikipediaURL(String message, Throwable cause) {
        super(message, cause);
    }
}
