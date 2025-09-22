package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动拥有基本的CRUD方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     * 用于登录验证和检查用户名是否存在
     * 
     * @param name 用户名
     * @return 用户对象，如果不存在返回null
     */
    @Select("SELECT * FROM ita_home.user WHERE name = #{name}")
    User findByName(String name);

    /**
     * 检查用户名是否存在
     * 用于注册时验证用户名是否重复
     * 
     * @param name 用户名
     * @return 存在返回true，不存在返回false
     */
    @Select("SELECT COUNT(*) > 0 FROM ita_home.user WHERE name = #{name}")
    boolean existsByName(String name);
}
