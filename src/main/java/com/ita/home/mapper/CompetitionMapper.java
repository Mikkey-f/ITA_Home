package com.ita.home.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ita.home.model.entity.Competition;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

/**
 * 竞赛Mapper接口
 * 继承MyBatis Plus的BaseMapper，自动拥有基本的CRUD方法
 */
@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {
}
