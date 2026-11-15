package com.theethancorporation.util;

public class PathUtils {
    public static String cleanseStringForWindows(String stringToCleanse) {
        return stringToCleanse.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
