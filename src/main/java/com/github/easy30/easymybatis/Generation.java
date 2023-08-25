package com.github.easy30.easymybatis;
public interface Generation<T>
{

	/**
	 *
	 * @param context
	 * @return
	 */
	 T generate(GenerationContext context);
	 

}
