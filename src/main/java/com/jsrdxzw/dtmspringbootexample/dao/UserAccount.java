package com.jsrdxzw.dtmspringbootexample.dao;

import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author xuzhiwei
 * @date 2022/4/8 23:18
 */
@TableName("user_account")
public class UserAccount {
    private Integer id;
    private Integer userId;
    private BigDecimal balance;
    private BigDecimal tradingBalance;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
