package com.cehome.easymybatis.test2;


import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.annotation.ColumnDefault;
import lombok.Data;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Table(name = "user")
public class User extends DialectEntity {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String realName;
    private String email;

    @ColumnDefault(insertValue = "now()")
    private Date createTime;

    @ColumnDefault("now()")
    private Date updateTime;


}
