package com.cehome.easymybatis.test1;


import com.cehome.easymybatis.annotation.Query;
import com.cehome.easymybatis.annotation.QueryColumn;
import com.cehome.easymybatis.annotation.QueryItem;
import com.cehome.easymybatis.enums.ColumnOperator;
import lombok.Data;

import java.util.Date;

/**
 * @selectBase
 */
@Data
@Query(columns = "id,createTime", where = "1=1 and {createTime} is not null ")
public class UserParams2 extends User {

    @QueryItem("create_time>= #{createTimeStart} ")
    private Date createTimeStart;

    @QueryItem("name like CONCAT('%',#{nameSuffix})")
    private String nameSuffix;

    //-- array default is IN , id in ( 1,2,3...)
    @QueryColumn(column ="id")
    private Long[] ids;

    //-- between
    @QueryColumn(column ="id",operator = ColumnOperator.BETWEEN)
    private Long[] idBetween;

    @QueryColumn(column = "name",operator =ColumnOperator.NULL )
    private Boolean nameNull;


}
