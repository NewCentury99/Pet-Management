package com.sju18.petmanagement.global.exception;

import com.sju18.petmanagement.global.util.error.ErrorCode;
import lombok.Getter;

@Getter
public class DtoValidityException extends Exception {
    private final ErrorCode errorCode = ErrorCode.DTO_VALIDATION;
    public DtoValidityException(String msg) {
        super(msg);
    }
}