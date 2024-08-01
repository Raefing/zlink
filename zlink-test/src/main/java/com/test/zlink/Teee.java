package com.test.zlink;


public class Teee {
    public static void main(String[] args) {
        System.err.println(isMatch("/platform/getPrivate**","/platform/getPrivate?teee=1"));
    }

    public static boolean isMatch(String white, String requestUrl) {

        int i = 0;
        int j = 0;
        int starIndex = -1;
        int iIndex = -1;
        while (i < requestUrl.length()) {
            if (j < white.length() && (white.charAt(j) == '?' || white.charAt(j) == requestUrl.charAt(i))) {
                i++;
                j++; continue;
            }  if (j < white.length() && white.charAt(j) == '*') {
                starIndex = j;
                iIndex = i;
                j++; continue;
            }  if (starIndex != -1) {
                j = starIndex + 1;
                i = iIndex + 1;
                iIndex++; continue;
            }
            return false;
        }

        while (j < white.length() && white.charAt(j) == '*') {
            j++;
        }
        return (j == white.length());
    }

}
