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
@Query(columns = "id,createTime",conditions = "1=1 and {createTime} is not null ")
public class UserParams2 extends User {

    @QueryItem("create_time>= #{createTimeStart} ")
    private Date createTimeStart;

    @QueryColumn(column ="id")
    private Long[] ids;

    @QueryColumn(column ="id",operator = ColumnOperator.BETWEEN)
    private Long[] idRange;


}
