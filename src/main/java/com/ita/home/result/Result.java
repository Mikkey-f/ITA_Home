package com.ita.home.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Mikkeyf
 * @CreateTime: 2025/9/22 15:48
 */
@Data
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {
    
    @Schema(description = "响应状态码", example = "1")
    private Integer code;       //编码：1成功，0和其他值失败
    
    @Schema(description = "响应消息", example = "操作成功")
    private String msg;         //错误信息
    
    @Schema(description = "响应数据")
    private T data;             //数据

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object){
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg){
        Result<T> result = new Result<T>();
        result.msg = msg;
        result.code = 0;
        return result;
    }
}
