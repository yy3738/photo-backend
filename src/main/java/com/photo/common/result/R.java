package com.photo.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 */
@Data
@Schema(description = "统一响应体")
public class R<T> implements Serializable {

    @Schema(description = "状态码，0表示成功")
    private int code;
    @Schema(description = "提示信息")
    private String message;
    @Schema(description = "响应数据")
    private T data;

    public static <T> R<T> ok() {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage("success");
        return r;
    }

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(0);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> fail(int code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
