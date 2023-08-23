package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    // 注意这里解析是 #,  yml中用的是 $
    @Select("select  * from user where openid =#{openid}")
     public  User getByOpenId(String openid) ;

}
