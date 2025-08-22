package com.github.easy30.easymybatis.test4;


import com.github.easy30.easymybatis.codegen.RandomObjectUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class EquipmentSms2Test  {
    @Autowired
    EquipmentSms2Mapper equipmentSms2Mapper;
    Integer id;

     @Test
     public void testAll(){

     }

    @Test
    public void add(){
        EquipmentSms2 equipmentSms2=new EquipmentSms2();
        RandomObjectUtil.initObject(equipmentSms2,"id");
        id=equipmentSms2Mapper.insert(equipmentSms2,null);
        System.out.println(equipmentSms2.getId());

    }



}
