package com.leyou.common.exception.controller;

import com.leyou.common.exception.pojo.ExceptionResult;
import com.leyou.common.exception.pojo.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 */
@ControllerAdvice
public class LyExceptionController {

    /**
     * 异常处理方法
     */
    @ExceptionHandler(value = LyException.class)   // 这个方法用于捕获LyException异常类型 注意：@ExceptionHandler捕获的异常类必须是RuntimeExxception或者Exception
    @ResponseBody  // 返回值转换为Json字符串
    public ResponseEntity<ExceptionResult> handlerException(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }
}
