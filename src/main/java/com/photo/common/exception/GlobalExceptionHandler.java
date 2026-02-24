package com.photo.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.photo.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleNotLogin(NotLoginException e) {
        return R.fail(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleNotRole(NotRoleException e) {
        return R.fail(403, "权限不足");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return R.fail(400, msg);
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBiz(BizException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(R.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "服务器内部错误");
    }
}
