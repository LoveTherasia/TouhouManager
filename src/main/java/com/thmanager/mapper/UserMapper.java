package com.thmanager.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.thmanager.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
