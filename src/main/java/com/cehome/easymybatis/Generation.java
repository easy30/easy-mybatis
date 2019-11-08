package com.cehome.easymybatis;


import javax.persistence.GeneratedValue;

public interface Generation<T>
{
     enum Type { NONE,VALUE, SELECT_KEY_SQL,SQL_VALUE }

     Type getType();
	 
	 T generate(Object entity,String property,String table,String arg);
	 

}
