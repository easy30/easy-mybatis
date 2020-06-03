package com.cehome.easymybatis.test1;


import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.annotation.ColumnDefault;
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "user")
public class User {

    @Id
    private Long id;
    private String name;
    private Integer age;
    private String realName;
    @ColumnDefault(insertValue = "now()")
    private Date createTime;

}
