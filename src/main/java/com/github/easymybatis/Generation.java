package com.github.easymybatis;


import javax.persistence.GeneratedValue;

public interface Generation<T>
{

	 T generate(Object entity,String property,String table,String arg);
	 

}
