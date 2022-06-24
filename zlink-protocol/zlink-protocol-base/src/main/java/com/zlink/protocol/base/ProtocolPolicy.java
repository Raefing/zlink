package com.zlink.protocol.base;

import org.apache.commons.lang3.StringUtils;

public class ProtocolPolicy {
    private int headLength;
    private int start;
    private int end;
    private int fixLength;

    public ProtocolPolicy(String policy) {
        if (StringUtils.isNotBlank(policy)) {
            int index = policy.indexOf(":");
            if (index > 0) {
                String type = policy.substring(0, index);
                String params = policy.substring(index + 1);
                if (type.equalsIgnoreCase("LENGTH")) {
                    String[] pas = params.split(",");
                    for (String pa : pas) {
                        if (pa.toUpperCase().startsWith("S=")) {
                            start = Integer.parseInt(pa.toUpperCase().replace("S=", ""));
                        } else if (pa.toUpperCase().startsWith("E=")) {
                            end = Integer.parseInt(pa.toUpperCase().replace("E=", ""));
                        }
                    }
                    if (start >= 0 && start <= end) {
                        headLength = end - start + 1;
                    }
                } else {
                    if (type.equalsIgnoreCase("FIX")) {
                        start = 0;
                        end = 0;
                        headLength = 0;
                        fixLength = Integer.parseInt(params);
                    } else {
                        headLength = 0;
                        start = 0;
                        end = 0;
                        fixLength = 0;
                    }
                }
            } else {
                headLength = 0;
                start = 0;
                end = 0;
                fixLength = 0;
            }
        } else {
            headLength = 0;
            start = 0;
            end = 0;
            fixLength = 0;
        }
    }

    public int getHeadLength() {
        return headLength;
    }

    public boolean isFix() {
        return fixLength > 0;
    }

    public int getContentLength(byte[] headData) {
        String str = new String(headData);
        return Integer.parseInt(str);
    }

    public int getFixLength() {
        return fixLength;
    }

    public byte[] createHeadBytes(int length) {
        String headStr = String.format("%0" + headLength + "d", length);
        return headStr.getBytes();
    }

    public static String UNKNOWN = "UNKNOWN";

    public static String FIX(int len) {
        return "FIX:" + len;
    }

    public static String LENGTH(int start, int end) {
        return "LENGTH:S=" + start + ",E=" + end;
    }

}
