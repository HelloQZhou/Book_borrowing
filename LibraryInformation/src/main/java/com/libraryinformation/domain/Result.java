package com.libraryinformation.domain;

import lombok.Data;

/**
 * 返回结果类
 *
 * @author ZhouTX
 */
@Data
public class Result {
    private Object data;
    private Integer code;
    private String msg;

    public Result() {
    }

    public Result(Integer code, Object data) {
        this.data = data;
        this.code = code;
    }

    public Result(Integer code, Object data, String msg) {
        this.data = data;
        this.code = code;
        this.msg = msg;
    }
}
