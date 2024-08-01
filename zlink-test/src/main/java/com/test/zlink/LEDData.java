package com.test.zlink;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class LEDData implements Serializable {
    public static int LED_COUNT = 60;
    public static int[] SDO_Buffer = new int[LED_COUNT];

    public static int _zhugeluo_index = 0;

    public static int[] xialuo(int index) {
        while (true) {
            int liang = (_zhugeluo_index / LED_COUNT) % LED_COUNT;
            int dy = _zhugeluo_index % LED_COUNT;
            if (LED_COUNT - 1 - dy > liang) {
                for (int i = 0; i < LED_COUNT; i++) {
                    if (i < liang) {
                        SDO_Buffer[i] = 0xff;
                    } else {
                        SDO_Buffer[i] = 0x80;
                    }
                }
                SDO_Buffer[LED_COUNT - 1 - dy] = 0xff;
                _zhugeluo_index++;
                break;
            }
            _zhugeluo_index++;
        }
        return SDO_Buffer;
    }

    public static int _shangsheng_index = 0;

    public static int[] shangsheng(int index) {
        while (true) {
            int liang = (_shangsheng_index / LED_COUNT) % LED_COUNT;
            int dy = _shangsheng_index % LED_COUNT;
            if (dy < LED_COUNT - 1 - liang) {
                for (int i = 0; i < LED_COUNT; i++) {
                    if (i > LED_COUNT - 1 - liang) {
                        SDO_Buffer[i] = 0xff;
                    } else {
                        SDO_Buffer[i] = 0x80;
                    }
                }
                SDO_Buffer[dy] = 0xff;
                _shangsheng_index++;
                break;
            }
            _shangsheng_index++;
        }
        return SDO_Buffer;
    }

    public static int[] koushan(int index) {
        int i, l = 0, r = 0, dx = 8;
        l = LED_COUNT - 1 - index;
        r = index;
        for (i = 0; i < LED_COUNT; i++) {
            SDO_Buffer[i] = 0x80;
        }
        doPart(l, dx);
        doPart(r, dx);
        return SDO_Buffer;
    }

    public static void doPart(int i, int dx) {
        if (i - dx > 0) {
            doLine(i - dx);
        } else {
            doLine(LED_COUNT - dx);
        }
        doLine(i);
        if (i + dx < LED_COUNT) {
            doLine(i + dx);
        } else {
            doLine(i + dx - LED_COUNT);
        }
    }

    public static void doLine(int i) {
        if (i - 2 >= 0) {
            SDO_Buffer[i - 2] = 0x88;
        }
        if (i - 1 >= 0) {
            SDO_Buffer[i - 1] = 0xf0;
        }
        SDO_Buffer[i] = 0xff;
        if (i + 1 < LED_COUNT - 1) {
            SDO_Buffer[i + 1] = 0xf0;
        }
        if (i + 2 < LED_COUNT - 1) {
            SDO_Buffer[i + 2] = 0x88;
        }
    }

    static int flag = 1;

    public static int[] fantan(int index) {
        for (int i = 0; i < LED_COUNT; i++) {
            SDO_Buffer[i] = 0x80;
        }
        if (flag == 1) {
            doLine(index);
        } else {
            doLine(LED_COUNT - 1 - index);
        }
        if (index == LED_COUNT-1) {
            flag = ~flag;
        }
        SDO_Buffer[0] = 0xff;
        SDO_Buffer[LED_COUNT - 1] = 0xff;
        return SDO_Buffer;
    }

    static int flag1 = 1;
    static int flag2 = 1;
    short i;
    static int index2 = -8;
    public static int[] bolang(int index) {
        for (int i = 0; i < LED_COUNT; i++) {
            SDO_Buffer[i] = 0x80;
        }
        if (flag1 == 1) {
            doLine(index);
        } else {
            doLine(LED_COUNT - 1 - index);
        }
        if (index == LED_COUNT-1) {
            flag1 = ~flag1;
        }
        if (index2 >= 0){
            if (flag2 == 1) {
                doLine(index2);
            } else {
                doLine(LED_COUNT - 1 - index2);
            }
        }
        if (index2 == LED_COUNT-1) {
            flag2 = ~flag2;
            index2 = 0;
        }else {
            index2++;
        }
        return SDO_Buffer;
    }

}
