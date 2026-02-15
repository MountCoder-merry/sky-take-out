package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import com.sky.entity.User;

import java.util.Map;

@Mapper
public interface UserMapper {
 
 
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
        public User getByOpenid(String openid);
    
    /**
     * 查入数据
     * @param user
     */
    void insert(User user);

    /**
     * 根据id查询用户
     * @param userId
     * @return
     */
    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    /**
     * 根据条件统计用户数量
     * @param map
     * @return
     */
    //@Select("select count(*) from user where create_time between #{beginTime} and #{endTime}")
    Integer countByMap(Map map);
}