package com.xsdhy.captcha.common;


import lombok.Data;

/**
 * @author zhongping
 */

@Data
public class BizException extends RuntimeException{
    private String msg;
    private Integer code = 5000;

    public BizException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BizException(String msg, Integer code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public BizException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public BizException(Integer code, String msg, Throwable e) {
        super(msg, e);
        this.code = code;
        this.msg = msg;
    }



}
