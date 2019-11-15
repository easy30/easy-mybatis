package com.cehome.easymybatis.test2;

import com.cehome.easymybatis.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * coolma 2019/10/23
 **/
public interface UserMapper2 extends Mapper<User> {
    @Select("select * from user where id = #{id}")
    User findById(@Param("id") long id);

    User findById2(long id);

}