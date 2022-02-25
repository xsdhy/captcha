package com.xsdhy.captcha.common;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;


@Data
@Accessors(chain = true)
@Slf4j
public class Resp<T> implements Serializable {
    private int code=2000;

    private String msg;

    private T data;

    /**
     * 构造方法私有化，不允许外部new Result
     */
    private Resp(T data) {
        this.msg = null;
        this.data = data;
    }

    public Resp(String msg) {
        this.code=5000;
        this.msg = msg;
    }
    public Resp() {
    }



    /**
     * 成功时调用
     */
    public static <U> Resp<U> ok(U data) {
        return new Resp<>(data);
    }

    /**
     * 成功时调用 返回不带数据
     */
    public static Resp<Void> ok() {
        return new Resp<>();
    }



    public static Resp<Void> error(String msg) {
        return new Resp<>(msg);
    }


}
