package com.jsrdxzw.dtmspringbootexample.controller.model;

import lombok.Data;

/**
 * @author xuzhiwei
 * @date 2022/4/9 16:43
 */
@Data
public class ResultData {
    private int code;
    private String message;

    public static ResultData success() {
        ResultData resultData = new ResultData();
        resultData.setCode(0);
        resultData.setMessage("ok");
        return resultData;
    }
}
