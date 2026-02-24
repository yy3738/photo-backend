package com.photo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final HttpStatus httpStatus;

    public BizException(String message) {
        super(message);
        this.code = 400;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BizException(int code, String message, HttpStatus httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    /** 400 参数错误 */
    public static BizException badRequest(String message) {
        return new BizException(400, message, HttpStatus.BAD_REQUEST);
    }

    /** 404 资源不存在 */
    public static BizException notFound(String message) {
        return new BizException(404, message, HttpStatus.NOT_FOUND);
    }

    /** 409 业务冲突 */
    public static BizException conflict(String message) {
        return new BizException(409, message, HttpStatus.CONFLICT);
    }

    /** 403 权限不足 */
    public static BizException forbidden(String message) {
        return new BizException(403, message, HttpStatus.FORBIDDEN);
    }
}
