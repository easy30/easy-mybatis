package com.cehome.easymybatis;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * coolma 2019/10/23
 **/
public interface UserMapper extends Mapper<User> {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User getUser(@Param("id") Long id);


    @Select("select * from user where id = #{id}")
    User findById(@Param("id") long id);

    @Select("SELECT * FROM user where id>#{id}")
    List<User> getPage(@Param("id") long id,RowBounds rowBounds);

}