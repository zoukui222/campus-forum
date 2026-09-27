package com.zwz.forum.common.exception;

import lombok.Getter;

/**
 * @Author: ljx
 * @Date: 2025/11/21 13:46
 */
@Getter
public class BusinessException extends RuntimeException{
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
