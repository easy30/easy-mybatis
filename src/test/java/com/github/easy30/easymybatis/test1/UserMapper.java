package com.github.easy30.easymybatis.test1;

import com.github.easy30.easymybatis.Mapper;
import com.github.easy30.easymybatis.Const;
import com.github.easy30.easymybatis.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.session.RowBounds;

import java.util.List;

/**
 * coolma 2019/10/23
 **/
public interface UserMapper extends Mapper<User,UserDto> {

    @Select("select * from user where id = #{id}")
    User getByIdWithAnnotation(@Param("id") Long id);

    @Select("SELECT * FROM user where id>#{id}")
    List<User> listWithAnnotation(@Param("id") Long id, RowBounds rowBounds);


    User getByIdWithXml(long id);

    //select * from user where  name=#{name} and age>=#{age}
    List<User> listWithXml(@Param("age") Integer age,@Param("name") String name);

    List<User> pageInXML(@Param("age") Integer age, @Param(Const.PAGE) Page page);

    void inserts();
}