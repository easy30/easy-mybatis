package com.cehome.easymybatis;


import com.cehome.easymybatis.annotation.ColumnDefault;

import java.util.Date;

import javax.persistence.*;
@Table(name = "user")
@Entity
public class User extends DialectEntity {
    //@TableId(value = "id", type = IdType.AUTO)
    @Id
   // @ColumnGenerator(name="")
    private Long id;
    private String name;
    private Integer age;
    private String realName;
    private String email;

    @ColumnDefault(insertValue = "now()")
    @Column(updatable = false)
    private Date createTime;

    @ColumnDefault("now()")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
