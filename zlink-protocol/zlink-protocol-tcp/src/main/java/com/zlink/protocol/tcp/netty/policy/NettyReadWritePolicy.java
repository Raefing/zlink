package com.zlink.protocol.tcp.netty.policy;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class NettyReadWritePolicy {

    private int headLength;
    private int start;
    private int end;
    private int fixLength;

    public NettyReadWritePolicy(String policy) {
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

    public int getBodyLength(byte[] headBytes) {
        String str = new String(headBytes);
        return Integer.parseInt(str);
    }

    public int getFixLength() {
        return fixLength;
    }

    public byte[] createHeadBytes(int length) {
        String headStr = String.format("%0" + headLength + "d", length);
        return headStr.getBytes();
    }
}
