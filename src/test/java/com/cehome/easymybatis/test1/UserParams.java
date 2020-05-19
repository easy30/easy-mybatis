package com.cehome.easymybatis.test1;


import com.cehome.easymybatis.annotation.QueryProperty;

/**
 * @selectBase
 */
public class UserParams extends User {

    @QueryProperty(" create_time>= #{time1} ")
    private Integer time1;
}
