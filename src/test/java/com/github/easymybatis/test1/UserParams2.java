package com.github.easymybatis.test1;


import com.github.easymybatis.Range;
import com.github.easymybatis.annotation.Query;
import com.github.easymybatis.annotation.QueryColumn;
import com.github.easymybatis.annotation.QueryItem;
import com.github.easymybatis.enums.ColumnOperator;
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

    @QueryColumn(column ="createTime",operator = ColumnOperator.GT)
    private Date createTimeStart2;

    @QueryColumn(column ="createTime",operator = ColumnOperator.LE)
    private Date createTimeEnd;

    @QueryItem("name like CONCAT('%',#{nameSuffix})")
    private String nameSuffix;

    //-- array default is IN , id in ( 1,2,3...)
    @QueryColumn(column ="id")
    private Long[] ids;

    @QueryColumn(column ="name")
    private String[] names;

    //-- between
    @QueryColumn(column ="id",operator = ColumnOperator.BETWEEN)
    private Long[] idBetween;

    @QueryColumn(column = "name",operator =ColumnOperator.NULL )
    private Boolean nameNull;

    @QueryColumn(column = "age")
    private Range ageRange;


}
