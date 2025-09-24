package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.entity.User;
import com.ita.home.model.entity.UserOj;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户OJ平台账号Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动拥有基本的CRUD方法
 */
@Mapper
public interface UserOjMapper extends BaseMapper<UserOj> {

    /**
     * 通过userId查找UserOj
     * @param userId 用户id输入
     * @return 返回UserOj
     */
    @Select("SELECT * FROM ita_home.user_oj WHERE user_id = #{userId}")
    UserOj findByUserId(Long userId);
}