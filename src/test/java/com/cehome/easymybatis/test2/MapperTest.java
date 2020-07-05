package com.cehome.easymybatis.test2;

import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class MapperTest {

    @Autowired
    UserMapper2 userMapper2;

    Long id;

    @Before
    public void before(){
        setId();
    }
    private void setId(){
        id = userMapper2.getValueByCondition( null, null,"max(id)");
    }

    @Test
    public void  testAll()  {
        getById();
        getByIdAnno();
        findByIdXml();
        list();

    }

    @Test
    public void getById() {

        User user= userMapper2.getById(id,null);
        Assert.assertEquals(id,user.getId());
    }


    @Test
    public void getByIdAnno()   {

        User user= userMapper2.getByIdAnno(id);
        Assert.assertEquals(id,user.getId());
    }
    @Test
    public void findByIdXml()   {

        User user= userMapper2.getByIdXml(id);
        Assert.assertEquals(id,user.getId());
    }

    @Test
    public void list()  {

        List<User> list=userMapper2.list(1,new RowBounds(0,2));
        Assert.assertTrue(list.size()>0);
    }

}