package com.cehome.easymybatis.test2;

import com.alibaba.fastjson.JSON;
import com.cehome.easymybatis.Page;
import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class MapperTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    UserMapper2 userMapper2;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    Long id=36L;
    Integer age=25;
    String name="ma";
    String realName="coolma";

    @Test
    public void testAll() throws SQLException {
        testDelete();
        testUpdate();
        testSelect();
    }
    @Test
    public void testSelect() throws SQLException {
        getByEntity();
        getUser();
        getById();
        getValueByEntity();
        getValueByWhere();
        listByEntity();
        listBySQL();
        pageByEntity();
        pageBySQL();
    }

    @Test
    public void testUpdate() throws SQLException {
        insert();
        update();
        updateByEntity();
        updateByWhere();

    }

    @Test
    public void testDelete() throws SQLException {
        insert();delete();
        insert();deleteById();
        insert();deleteByEntity();
        insert();deleteByWhere();

    }

    @Test
    public void test() throws SQLException {
        User user=null;

        //-- update user set name='coolma', update_time=now() where id=100
        user=new User();
        user.setName("coolma");
        user.setId(100L);
        user.setValue("updateTime","now()");
        userMapper2.update(user);


    }
    @Test
    public void insert() throws SQLException {
        User user=new User();
        user.setName(name);
        user.setAge(age);
        user.setRealName(realName);
        user.setValue("email", "'a@a.com'");
        userMapper2.insert(user);

        System.out.println(JSON.toJSONString(user));
        Assert.assertNotNull(user.getId());
        id=user.getId();
    }
    @Test
    public void update() throws SQLException {
        User user=new User();
        user.setName("updateById");
        user.setId(id);
        user.setValue("createTime","now()");
        Assert.assertEquals(1, userMapper2.update(user));
    }
    @Test
    public void updateByEntity() throws SQLException {

        User user=new User();
        user.setName("updateByEntity");
        user.setCreateTime(new Date());
        user.setEmail("ube@a.com");

        User where=new User();
        where.setId(id);
        where.setAge(age);
        Assert.assertEquals(1, userMapper2.updateByEntity(user,where));

    }
    @Test
    public void updateByWhere() throws SQLException {

        User user=new User();
        user.setCreateTime(new Date());
        user.setEmail("usa@a.com");

        String where="{id}=#{id} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("id",id);
        map.put("realName",realName);

        int row= userMapper2.updateByWhere(user,where,map);
        System.out.println(row);

    }

    @Test
    public void delete() throws SQLException {
        //System.out.println(dataSource.getConnection().getMetaData().getURL());
        User user=new User();
        user.setId(id);
        Assert.assertEquals(1, userMapper2.delete(user));

    }
    @Test
    public void deleteById() throws SQLException {
        Assert.assertEquals(1, userMapper2.deleteById(id));


    }
    @Test
    public void deleteByEntity() throws SQLException {

        User params=new User();
        params.setName(name);
        params.setAge(age);
        Assert.assertEquals(1, userMapper2.deleteByEntity(params));


    }
    @Test
    public void deleteByWhere() throws SQLException {


        String where="{name}=#{name} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("name",name);
        map.put("realName",realName);

        int row= userMapper2.deleteByWhere(where,map);
        Assert.assertEquals(1,row);

    }


    @Test
    public void findById() throws SQLException {

        User user= userMapper2.findById(36L);
        System.out.println(JSON.toJSONString(user));
    }
    @Test
    public void findById2() throws SQLException {

        User user= userMapper2.findById2(36L);
        System.out.println(JSON.toJSONString(user));
    }

    @Test
    public void getByEntity() throws SQLException {
        User params=new User();
        params.setId(id);
        User user= userMapper2.getByEntity(params,null);
        verify(user,id);

    }

    @Test
    public void getUser() throws SQLException {
         User user= userMapper2.getUser(id);
        verify(user,id);
    }
    private void verify(User user, Long id){
        System.out.println(JSON.toJSONString(user));
        Assert.assertEquals(user.getId(),id);
        Assert.assertNotNull(user.getName());
    }

    @Test
    public void getById() throws SQLException {
        //System.out.println(dataSource.getConnection().getMetaData().getURL());

        User user= userMapper2.getById(id,null);
        verify(user,id);
    }



    @Test
    public void getValueByEntity() throws SQLException {
        User params=new User();
        params.setId(id);
        Object value= userMapper2.getValueByEntity(params,"name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);


    }
    @Test
    public void getValueByWhere() throws SQLException {
        {
            User params = new User();
            params.setId(36L);
            Object value = userMapper2.getValueByWhere( "{id}=#{id}", params,"name");
            System.out.println(JSON.toJSONString(value));
            Assert.assertNotNull(value);
        }

        {
            Map<String,Object> params=new HashMap();
            params.put("id",36L);
            Object value = userMapper2.getValueByWhere( "{id}=#{id}", params,"name");
            System.out.println(JSON.toJSONString(value));
            Assert.assertNotNull(value);
        }

    }

    @Test
    public void listByEntity() throws SQLException {
        User params=new User();
        params.setAge(20);
        List<User> list= userMapper2.listByEntity(params," name asc, createTime desc","age,createTime");
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void pageByEntity() throws SQLException {
        User params=new User();
        params.setAge(20);
        Page<User> page=new Page(1,3);
        List<User> list= userMapper2.pageByEntity(params,page," name asc, createTime desc","age,createTime");
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }

    @Test
    public void listBySQL() throws SQLException {
        User params=new User();
        params.setAge(20);
        List<User> list= userMapper2.listBySQL(" age>#{age} order by {createTime} desc",params);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void pageBySQL() throws SQLException {
        User params=new User();
        params.setAge(20);
        Page<User> page=new Page(2,5);
        List<User> list= userMapper2.pageBySQL(" age>#{age} order by {createTime} desc",params,page);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(list.size()>0);
    }



}