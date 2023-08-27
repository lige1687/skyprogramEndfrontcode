package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    // 注意这里解析是 #,  yml中用的是 $
    @Select("select  * from user where openid =#{openid}")
     public  User getByOpenId(String openid) ;

    void insert(User user);

    @Select("select * from user where id= #{id} ")
    User getById(Long userId);
    // 注意insert  需要返回 一个 主键以供 其他方法的使用, 所以此时需要使用 xml配置文件进行sql 的书写
}
