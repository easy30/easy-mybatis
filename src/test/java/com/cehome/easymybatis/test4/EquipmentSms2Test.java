package com.cehome.easymybatis.test4;


import com.cehome.easymybatis.codegen.RandomObjectUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class EquipmentSms2Test  {
    @Resource
    EquipmentSms2Mapper equipmentSms2Mapper;
    Integer id;

     @Test
     public void testAll(){

     }

    @Test
    public void add(){
        EquipmentSms2 equipmentSms2=new EquipmentSms2();
        RandomObjectUtil.initObject(equipmentSms2,"id");
        id=equipmentSms2Mapper.insert(equipmentSms2);
        System.out.println(equipmentSms2.getId());

    }



}
