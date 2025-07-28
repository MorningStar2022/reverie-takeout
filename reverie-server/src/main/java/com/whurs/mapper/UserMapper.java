package com.whurs.mapper;

import com.whurs.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询对应记录
     * @param openid
     * @return
     */
    @Select("select * from  user where openid=#{openid}")
    User getByOpenid(String openid);

    /**
     * 插入新注册用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据用户id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id=#{userId}")
    User getById(Long userId);

    /**
     * 动态查询截止某日总用户数量及当日新增用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
