package com.ita.home.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/23 17:10
 */
@Component
public class ValidateUtil {
    /**
     * 随机生成验证码
     * @param length 长度为4位或者6位
     * @return 返回验证码
     */
    public Integer generateValidateCode(int length){
        Integer code = null;
        if (length == 4) {
            code = new Random().nextInt(9999);
            if(code < 1000){
                code = code + 1000;
            }
        } else if (length == 6) {
            code = new Random().nextInt(999999);
            if(code < 100000){
                code = code + 100000;
            }
        } else {
            throw new RuntimeException("构建验证码失败");
        }
        return code;
    }
}
