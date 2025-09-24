package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.entity.UserOj;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户OJ平台账号Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动拥有基本的CRUD方法
 */
@Mapper
public interface UserOjMapper extends BaseMapper<UserOj> {

}