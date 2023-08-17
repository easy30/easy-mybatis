package com.github.easy30.easymybatis;


import javax.persistence.GeneratedValue;

public interface Generation<T>
{

	 /**
	 *
 	 * @param entity  实体类
	 * @param property 实体类属性
	 * @param table  表名
	 * @param arg  ColumnGeneration注解上的参数
	 * @return
	 */
	 T generate(Object entity,String property,String table,String arg);
	 

}
