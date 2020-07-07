package com.cehome.easymybatis.test1;

import com.alibaba.fastjson.JSON;
import com.cehome.easymybatis.Page;
import com.cehome.easymybatis.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class MapperTest1 {

    private static Logger logger = LoggerFactory.getLogger(MapperTest1.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    UserMapper1 userMapper1;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    Long id;
    Integer age=25;
    String name="ma";
    String realName="coolma";

    @Before
    public void before(){
        setId();
    }

    @Test
    public void testAll()   {
        long t=System.currentTimeMillis();
        name="ma"+t;
        realName=realName+t;

        testDelete();
        testUpdate();
        testSelect();
    }
    @Test
    public void testSelect()   {


        setId();

        getByParams();
        getByParamsReturnFirst();
        getById();
        getValueByParams();
        getValueByCondition();
        listByParams();
        listBySQL();
        pageByParams();
        pageByParamsNullOrEmpty();
        pageBySQL();
        queryItem();
        queryColumn();
        queryColumnRange();
    }

    @Test
    public void testUpdate()   {
        insert();
        update();
        updateByEntity();
        updateByCondition();

    }

    @Test
    public void testDelete()   {

        insert();deleteById();
        insert();deleteByEntity();
        insert();deleteByCondition();

    }

    @Test
    public void insert()   {
        User user=doInsert();
        id=user.getId();
    }

    private User createUser(){
        User user = new User();
        user.setName(name);
        user.setAge(age);
        user.setRealName(realName);
        return user;
    }

    public User doInsert()   {
        User user = null;
        //-- insert into user(name,age,real_name) values('coolma',20,'mike')
        user = new User();
        user.setName(name);
        user.setAge(age);
        user.setRealName(realName);
        userMapper1.insert(user);
        return user;
    }

    /**
     *
     *
     *
     * @ 
     */
    @Test
    public void test()   {
        User user = null;
        //-- insert into user(name,age,real_name) values('coolma',20,'mike')
        user = new User();
        user.setName("coolma");
        user.setAge(20);
        user.setRealName("mike");
        user.setId(100L);
        userMapper1.insert(user);
        Long id = user.getId(); //return 100

        //-- update user set real_name='michael' where id=100
        user = new User();
        user.setRealName("michael");
        user.setId(100L);
        userMapper1.update(user);

        //-- update user set real_name='tom where id=100 and age=20
        user = new User();
        user.setRealName("tom");
        User params = new User();
        params.setId(100L);
        params.setAge(20);
        userMapper1.updateByParams(user, params);

        //-- delete from user where id=100
        userMapper1.deleteById(100L);


        //-- select one: select * from user where id=100
        user= userMapper1.getById(id,null);

        //-- select one: select * from user where real_name='tom'
        params=new User();
        params.setRealName("tom");
        user= userMapper1.getByParams(params,null,null);

        //-- list: select name,real_name from user where age=20
        params=new User();
        params.setAge(20);
        List<UserDto> list=userMapper1.listByParams(params,null,"name,realName");

        //-- page: select name,real_name from user where age=20 order by name asc limit 0,20
        params=new User();
        params.setAge(20);
        Page<User> page=new Page(1,20);
        userMapper1.pageByParams(params,page,"name asc","name,realName");
        System.out.println(page.getData());

    }
    @Test
    public void update()   {
        User user=new User();
        user.setName("updateById");
        user.setId(id);
        //user.setValue("createTime","now()");
        Assert.assertEquals(1, userMapper1.update(user));
    }
    @Test
    public void updateByEntity()   {

        User user=new User();
        user.setName("updateByEntity");
        //user.setCreateTime(new Date());
        //user.setEmail("ube@a.com");

        User where=new User();
        where.setId(id);
        where.setAge(age);
        Assert.assertEquals(1, userMapper1.updateByParams(user,where));

    }
    @Test
    public void updateByCondition()   {

        User user=new User();
        user.setCreateTime(new Date(new Date().getTime()-1000000));


        String where="{id}=#{id} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("id",id);
        map.put("realName",realName);

        int row= userMapper1.updateByCondition(user,where,map);
        System.out.println(row);

    }


    @Test
    public void deleteById()   {
        deleteById(id);

    }
    private void deleteById(Long id)   {
        Assert.assertEquals(1, userMapper1.deleteById(id));

    }
    @Test
    public void deleteByEntity()   {

        User params=new User();
        params.setName(name);
        params.setAge(age);
        Assert.assertEquals(1, userMapper1.deleteByParams(params));


    }
    @Test
    public void deleteByCondition()   {


        String where="{name}=#{name} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("name",name);
        map.put("realName",realName);

        int row= userMapper1.deleteByCondition(where,map);
        Assert.assertEquals(1,row);

    }

    private void setId(){
        id = userMapper1.getValueByCondition( null, null,"max(id)");
    }


    @Test
    public void findById()   {

        //User user=userMapper.findById(36L);
        //System.out.println(JSON.toJSONString(user));
    }


    @Test
    public void getByParams()   {
        User params=new User();
        params.setId(id);
        User user= userMapper1.getByParams(params,"id desc",null);
        verify(user,id);

    }

    @Test
    public void getByParamsReturnFirst()   {
        User user1=doInsert();
        User user2=doInsert();
        User params=new User();
        params.setAge(age);
        User user= userMapper1.getByParams(params,"id desc",null);
        verify(user,user2.getId());
        userMapper1.deleteById(user1.getId());
        userMapper1.deleteById(user2.getId());

        params.setAge(1024);
        user= userMapper1.getByParams(params,"id desc",null);
        Assert.assertNull(user);


    }

 
    private void verify(User user, Long id){
        System.out.println(JSON.toJSONString(user));
        Assert.assertEquals(user.getId(),id);
        Assert.assertNotNull(user.getName());
    }

    @Test
    public void getById()   {
        //System.out.println(dataSource.getConnection().getMetaData().getURL());

        User user= userMapper1.getById(id,null);
        verify(user,id);
    }



    @Test
    public void getValueByParams()   {
        User params=new User();
        params.setId(id);
        Object value= userMapper1.getValueByParams(params,null,null);//"name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);


    }
    @Test
    public void getValueByCondition() {

        User params = new User();
        params.setId(id);
        Object value = userMapper1.getValueByCondition("{id}=#{id}", params, "name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);

        value = userMapper1.getValueByCondition("where {id}=#{id}", params, "name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);
        value = userMapper1.getValueByCondition("order by id desc", null, "name");
        Assert.assertNotNull(value);
        value = userMapper1.getValueByCondition("ORDER by id desc", null, "name");
        Assert.assertNotNull(value);
        value = userMapper1.getValueByCondition("group by id order by id desc", null, "name");
        Assert.assertNotNull(value);
        value = userMapper1.getValueByCondition(null, null, "name");
        Assert.assertNotNull(value);
        value = userMapper1.getValueByCondition("", null, "name");
        Assert.assertNotNull(value);

    }

    @Test
    public void listByParams()   {
        User params=new User();
        params.setAge(20);
        List<UserDto> list= userMapper1.listByParams(params," name asc, createTime desc","age,createTime");
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void pageByParamsNullOrEmpty()   {

        Page<User> page=new Page(1,3);
        List<UserDto> list=null;
        list= userMapper1.pageByParams(null,page," name asc, createTime desc","age,createTime");
        list= userMapper1.pageByParams(new User(),page," name asc, createTime desc","age,createTime");

    }


    @Test
    public void pageByParams()   {
        User params=new User();
        params.setAge(20);
        Page<User> page=new Page(1,3);
        List<UserDto> list= userMapper1.pageByParams(params,page," name asc, createTime desc","age,createTime");
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }

    @Test
    public void listBySQL()   {
        User params=new User();
        params.setAge(20);
        List<UserDto> list= userMapper1.listBySQL(" age>#{age} order by {createTime} desc",params);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void pageBySQL()   {
        User params=new User();
        params.setAge(age);
        Page<UserDto> page=new Page(1,2);
        userMapper1.pageBySQL(" age>=#{age} order by {createTime} desc",params,page);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

        page=new Page(2,2);
        userMapper1.pageBySQL(" age>=#{age} order by {createTime} desc",params,page);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);
    }

    @Test
    public void listByParams2()   {
        UserParams params=new UserParams();
        params.setAge(20);
        List<UserDto> list= userMapper1.listByParams(params," name asc, createTime desc","age,createTime");
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void queryItem(){
        insert();
        try {
            User user = userMapper1.getById(id, null);
            System.out.println(id+","+user.getCreateTime());
            UserParams2 params = new UserParams2();
            params.setId(id);
            params.setCreateTimeStart(user.getCreateTime());
            User user1 = userMapper1.getByParams(params, "id desc", null);
            Assert.assertEquals(id,user1.getId());

            params = new UserParams2();
            params.setCreateTimeStart2(user.getCreateTime());
            Assert.assertNull(userMapper1.getByParams(params, null, null));

            params = new UserParams2();
            params.setCreateTimeEnd(user.getCreateTime());
            User user2 = userMapper1.getByParams(params, "id desc", null);
            Assert.assertEquals(id,user2.getId());

        }finally {
            deleteById();
        }

    }

    @Test
    public void queryColumn(){
        List<Long> ids=new ArrayList<>();
        insert();
        ids.add(id);
        insert();
        ids.add(id);

        UserParams2 params=new UserParams2();
        params.setIds(ids.toArray(new Long[0]));
        List<UserDto> list=userMapper1.listByParams(params,"id",null);
        Assert.assertEquals(2,list.size());
        Assert.assertEquals(list.get(0).getId(),ids.get(0));
        Assert.assertEquals(list.get(1).getId(),ids.get(1));

        params=new UserParams2();
        params.setIdBetween(ids.toArray(new Long[0]));
        params.setNameNull(false);
        list=userMapper1.listByParams(params,"id",null);
        Assert.assertEquals(2,list.size());
        Assert.assertEquals(list.get(0).getId(),ids.get(0));
        Assert.assertEquals(list.get(1).getId(),ids.get(1));

        deleteById(ids.get(0));
        deleteById(ids.get(1));
    }
    @Test
    public void queryColumnRange(){
        User user1=createUser();
        user1.setAge(80);
        userMapper1.insert(user1);

        User user2=createUser();
        user2.setAge(90);
        userMapper1.insert(user2);
        try {
            UserParams2 userParams2 = new UserParams2();
            Range range = Range.inRange(70, 95, true, true);
            userParams2.setAgeRange(range);

            long count = userMapper1.getValueByParams(userParams2, null, "count(*)");
            Assert.assertEquals(2,count);

        }finally {
            userMapper1.deleteById(user1.getId());
            userMapper1.deleteById(user2.getId());
        }



    }


    @Test
    public void pageByParams2()   {
        UserParams2 params=new UserParams2();
        //params.setAge(20);
        params.setNameSuffix("%mer");
        Page<User> page=new Page(1,3);
        List<UserDto> list= userMapper1.pageByParams(params,page," name asc, createTime desc","age,createTime");
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }


}