package com.github.easy30.easymybatis.test1;

import com.alibaba.fastjson.JSON;
import com.github.easy30.easymybatis.Range;
import com.github.easy30.easymybatis.Page;
import com.github.easy30.easymybatis.UpdateOption;
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
public class MapperTest {

    private static Logger logger = LoggerFactory.getLogger(MapperTest.class);

    @Autowired
    DataSource dataSource;

    @Autowired
    UserMapper userMapper;

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
        listWithXml();
        pageByParams();
        pageByParamsNullOrEmpty();
        pageInXML();
        pageByParamsIn();
        pageBySQL();
        queryItem();
        queryColumn();
        queryColumnRange();
    }

    @Test
    public void testUpdate()   {
        insert();
        update();
        updateByParams();
        updateByCondition();
        updateWithNull();
    }

    @Test
    public void testDelete()   {

        insert();deleteById();
        insert();deleteByCondition();
        deleteByParams();

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
        userMapper.insert(user);
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
        userMapper.insert(user,null);
        Long id = user.getId(); //return 100

        //-- update user set real_name='michael' where id=100
        user = new User();
        user.setRealName("michael");
        user.setId(100L);
        userMapper.update(user,null);

        //-- update user set real_name='tom where id=100 and age=20
        user = new User();
        user.setRealName("tom");
        User params = new User();
        params.setId(100L);
        params.setAge(20);
        userMapper.updateByParams(user, params,"id,age",null);

        //-- delete from user where id=100
        userMapper.deleteById(100L,null);


        //-- select one: select * from user where id=100
        user= userMapper.get(id,null,null);

        //-- select one: select * from user where real_name='tom'
        params=new User();
        params.setRealName("tom");
        user= userMapper.getByParams(params,null,null,null);

        //-- list: select name,real_name from user where age=20
        params=new User();
        params.setAge(20);
        List<UserDto> list= userMapper.listByParams(params,null,"name,realName",null);

        //-- page: select name,real_name from user where age=20 order by name asc limit 0,20
        params=new User();
        params.setAge(20);
        Page<User> page=new Page(1,20);
        userMapper.pageByParams(params,page,"name asc","name,realName",null);
        System.out.println(page.getData());

    }
    @Test
    public void update()   {
        User user=new User();
        user.setName("updateById");
        user.setRealName("realName");
        user.setId(id);
        //user.setValue("createTime","now()");
        Assert.assertEquals(1, userMapper.update(user));
    }

    @Test
    public void updateWithNull()   {
        System.out.println("update realName");
        User user=new User();
        user.setRealName("realName");
        user.setId(id);
        Assert.assertEquals(1, userMapper.update(user));
        user= userMapper.get(id,null);
        Assert.assertNotNull(user.getRealName());

        System.out.println("set realName=null... withNullColumns option");
        user=new User();
        user.setName("updateById");
        user.setId(id);
        Assert.assertEquals(1, userMapper.update(user,UpdateOption.create().withNullColumns(true)));
        user= userMapper.get(id,null);
        Assert.assertEquals(null,user.getRealName());


    }

    @Test
    public void updateByParams(){
        User params=doInsert();
        User user = new User();

        user.setRealName("tom");
        user.setName("mike");
        Assert.assertEquals(1,  userMapper.updateByParams(user, params,"id,age",null));
        User user2=userMapper.get(params.getId(),null,null);
        Assert.assertEquals("tom",user2.getRealName());
        Assert.assertEquals("mike",user2.getName());

        logger.info("ignore realName");
        user.setRealName("tom2");
        user.setName("mike2");
        Assert.assertEquals(1,  userMapper.updateByParams(user, params,"id,age", UpdateOption.create().ignoreColumns("realName")));
        user2=userMapper.get(params.getId(),null,null);
        Assert.assertEquals("tom",user2.getRealName());
        Assert.assertEquals("mike2",user2.getName());

        logger.info("extra fields");
        user.setRealName("tom3");
        user.setName("mike3");
        Assert.assertEquals(1,  userMapper.updateByParams(user, params,"id,age", UpdateOption.create().columnAndValues("realName","'tom4'")));
        user2=userMapper.get(params.getId(),null,null);
        Assert.assertEquals("tom4",user2.getRealName());
        Assert.assertEquals("mike3",user2.getName());

        Assert.assertEquals(1,  userMapper.deleteByParams(params,"id,age",null));

    }

    @Test
    public void updateByCondition()   {

        User user=new User();
        user.setCreateTime(new Date(new Date().getTime()-1000000));


        String where="{id}=#{id} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("id",id);
        map.put("realName",realName);

        int row= userMapper.updateByCondition(user,where,map,null);
        System.out.println(row);

    }


    @Test
    public void deleteById()   {
        deleteById(id);

    }
    private void deleteById(Long id)   {
        Assert.assertEquals(1, userMapper.deleteById(id,null));

    }

    @Test
    public void deleteByCondition()   {


        String where="{name}=#{name} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("name",name);
        map.put("realName",realName);

        int row= userMapper.deleteByCondition(where,map,null);
        Assert.assertEquals(1,row);

    }

    @Test
    public void deleteByParams(){
        User params=doInsert();
        Assert.assertEquals(1,userMapper.deleteByParams( params,"id,age",null));
    }

    private void setId(){
        id = userMapper.getValueByCondition( null, null,"max(id)",null);
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
        User user= userMapper.getByParams(params,"id desc",null,null);
        verify(user,id);

    }

    @Test
    public void getByParamsReturnFirst()   {
        User user1=doInsert();
        User user2=doInsert();
        User params=new User();
        params.setAge(age);
        User user= userMapper.getByParams(params,"id desc",null,null);
        verify(user,user2.getId());
        userMapper.deleteById(user1.getId(),null);
        userMapper.deleteById(user2.getId(),null);

        params.setAge(1024);
        user= userMapper.getByParams(params,"id desc",null,null);
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

        User user= userMapper.get(id,null,null);
        verify(user,id);
    }



    @Test
    public void getValueByParams()   {
        User params=new User();
        params.setId(id);
        Object value= userMapper.getValueByParams(params,null,null,null);//"name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);


    }
    @Test
    public void getValueByCondition() {

        User params = new User();
        params.setId(id);
        Object value = userMapper.getValueByCondition("{id}=#{id}", params, "name",null);
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);

        value = userMapper.getValueByCondition("where {id}=#{id}", params, "name",null);
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);
        value = userMapper.getValueByCondition("order by id desc", null, "name",null);
        Assert.assertNotNull(value);
        value = userMapper.getValueByCondition("ORDER by id desc", null, "name",null);
        Assert.assertNotNull(value);
        value = userMapper.getValueByCondition("group by id order by id desc", null, "name",null);
        Assert.assertNotNull(value);
        value = userMapper.getValueByCondition(null, null, "name",null);
        Assert.assertNotNull(value);
        value = userMapper.getValueByCondition("", null, "name",null);
        Assert.assertNotNull(value);

    }

    @Test
    public void listByParams()   {
        User params=new User();
        params.setAge(age);
        List<UserDto> list= userMapper.listByParams(params," name asc, createTime desc","age,createTime",null);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void pageByParamsNullOrEmpty()   {

        Page<User> page=new Page(1,3);
        List<UserDto> list=null;
        list= userMapper.pageByParams(null,page," name asc, createTime desc","age,createTime",null);
        list= userMapper.pageByParams(new User(),page," name asc, createTime desc","age,createTime",null);

    }


    @Test
    public void pageByParams()   {
        User params=new User();
        params.setAge(age);
        Page<User> page=new Page(1,3);
        List<UserDto> list= userMapper.pageByParams(params,page," name asc, createTime desc","age,createTime",null);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }
    @Test
    public void pageByParamsIn()   {
        List<Long> ids=new ArrayList<>();
        insert();
        ids.add(id);
        insert();
        ids.add(id);

        UserParams2 params=new UserParams2();
        params.setIds(ids.toArray(new Long[0]));
        Page page=new Page(1,2);
        userMapper.pageByParams(params,page,"id",null,null);
        Assert.assertEquals(2,page.getData().size());

    }
    @Test
    public void pageInXML()   {
        Page page= new Page(1,3);
        userMapper.pageInXML(2,page);
        Assert.assertEquals(3,page.getData().size());
    }


    @Test
    public void listBySQL()   {
        User params=new User();
        params.setAge(age-1);
        List<UserDto> list= userMapper.listBySQL(" age>#{age} order by {createTime} desc",params);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void pageBySQL()   {
        User params=new User();
        params.setAge(age);
        Page<UserDto> page=new Page(1,2);
        userMapper.pageBySQL(" age>=#{age} order by {createTime} desc",params,page);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

        page=new Page(2,2);
        userMapper.pageBySQL(" age>=#{age} order by {createTime} desc",params,page);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);
    }

    @Test
    public void listByParams2()   {
        UserParams params=new UserParams();
        params.setAge(age);
        List<UserDto> list= userMapper.listByParams(params," name asc, createTime desc","age,createTime",null);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void queryItem(){
        insert();
        try {
            User user = userMapper.get(id, null,null);
            System.out.println(id+","+user.getCreateTime());
            UserParams2 params = new UserParams2();
            params.setId(id);
            params.setCreateTimeStart(user.getCreateTime());
            User user1 = userMapper.getByParams(params, "id desc", null,null);
            Assert.assertEquals(id,user1.getId());

            params = new UserParams2();
            params.setCreateTimeStart2(user.getCreateTime());
            Assert.assertNull(userMapper.getByParams(params, null, null,null));

            params = new UserParams2();
            params.setCreateTimeEnd(user.getCreateTime());
            User user2 = userMapper.getByParams(params, "id desc", null,null);
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
        Page page=new Page(1,2);
        userMapper.pageByParams(params,page,"id",null,null);
        Assert.assertEquals(2,page.getData().size());

        List<UserDto> list= userMapper.listByParams(params,"id",null,null);
        Assert.assertEquals(2,list.size());
        Assert.assertEquals(list.get(0).getId(),ids.get(0));
        Assert.assertEquals(list.get(1).getId(),ids.get(1));

        params=new UserParams2();
        params.setIdBetween(ids.toArray(new Long[0]));
        params.setNameNull(false);
        list= userMapper.listByParams(params,"id",null,null);
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
        userMapper.insert(user1,null);

        User user2=createUser();
        user2.setAge(90);
        userMapper.insert(user2,null);
        try {
            UserParams2 userParams2 = new UserParams2();
            Range range = Range.inRange(70, 95, true, true);
            userParams2.setAgeRange(range);

            long count = userMapper.getValueByParams(userParams2, null, "count(*)",null);
            Assert.assertEquals(2,count);

        }finally {
            userMapper.deleteById(user1.getId(),null);
            userMapper.deleteById(user2.getId(),null);
        }



    }


    @Test
    public void pageByParams2()   {
        UserParams2 params=new UserParams2();
        //params.setAge(20);
        params.setNameSuffix("%mer");
        Page<User> page=new Page(1,3);
        List<UserDto> list= userMapper.pageByParams(params,page," name asc, createTime desc","age,createTime",null);
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }

    @Test
    public void listWithXml()   {
        User user1=createUser();
        user1.setName("ma"+System.currentTimeMillis());
        userMapper.insert(user1,null);
        List<User> list=userMapper.listWithXml(age,user1.getName());
        Assert.assertEquals(1,list.size());
        Assert.assertEquals(user1.getName(),list.get(0).getName());
        userMapper.deleteById(user1.getId(),null);

    }
    @Test
    public void inserts(){
        userMapper.inserts();
    }

   /* @Test
    public void listsql(){
        Map map=new HashMap();
        map.put("@@sql"," select * from ${u}");
        map.put("u","user");
        System.out.println( JSON.toJSONString( userMapper.list(map),true));
    }*/

}