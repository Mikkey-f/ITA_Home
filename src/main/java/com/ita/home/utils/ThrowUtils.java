package com.ita.home.utils;

import com.ita.home.exception.BaseException;

public class ThrowUtils {

    /**
     * 条件成立则抛异常
     *
     * @param condition 条件
     * @param message  错误信息
     */
    public static void throwIf(boolean condition, String  message) {
        if (condition) {
            throw new BaseException(message);
        }
    }
}