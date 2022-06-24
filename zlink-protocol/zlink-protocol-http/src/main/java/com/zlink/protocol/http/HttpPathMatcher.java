package com.zlink.protocol.http;

import org.springframework.util.AntPathMatcher;

public class HttpPathMatcher {
    private static AntPathMatcher antPathMatcher = new AntPathMatcher();

    public static boolean isMatch(String[] patterns, String path) {
        String testPath = path;
        if (testPath.indexOf("?") != -1) {
            int index = testPath.indexOf("?");
            testPath = testPath.substring(0, index);
        }
        for (String pattern : patterns) {
            if (antPathMatcher.match(pattern, testPath)) {
                return true;
            }
        }
        return false;
    }
}
