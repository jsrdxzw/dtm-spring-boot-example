package com.jsrdxzw.dtmspringbootexample.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jsrdxzw.dtmspringbootexample.dao.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author xuzhiwei
 * @date 2022/4/8 23:20
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccount> {

    /**
     * sagaAdjustBalance
     *
     * @param uid
     * @param amount
     */
    void sagaAdjustBalance(@Param("uid") Integer uid, @Param("amount") BigDecimal amount);

    /**
     * tccAdjustTrading
     *
     * @param uid
     * @param amount
     */
    void tccAdjustTrading(@Param("uid") Integer uid, @Param("amount") BigDecimal amount);

    /**
     * tccAdjustBalance
     *
     * @param uid
     * @param amount
     */
    void tccAdjustBalance(@Param("uid") Integer uid, @Param("amount") BigDecimal amount);
}
