package com.cehome.easymybatis.utils;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * 通过正则表达式来实现字符串替换（2008-08-21）
 * 可以实现动态地赋值替换、可以实现对匹配值的部分替换。
 * 例子查看main()函数
 * @version 1.0
 * @author coolma
 *
 *
 *
 */
public class RegularReplace
{

	private String content;
	private String regexp;
	private String format;
	Matcher m =null;
	StringBuffer buf = new StringBuffer();
	MessageFormat fmt =null;
	boolean b=false;
	Object[] values=null;
	int pointer=0;
	public RegularReplace(String content,String regexp)
	{
		this(content,regexp,"");
	}
	public RegularReplace(String content,String regexp,String format)
	{
		this.content=content;
		this.regexp=regexp;
		this.format=format;
		m=Pattern.compile(regexp).matcher(content);
		fmt = new MessageFormat(format);
		pointer=0;
	}

	public void reset()
	{
		m.reset();
		buf = new StringBuffer();
		pointer=0;
	}

	public String replaceAll(String newvalue)
	{
		return m.replaceAll(newvalue);
	}

	public String replaceAll(int group,String newvalue)
	{
		reset();
		while(find())
		{
			setGroup(group,newvalue);
			replace();

		}
		return getResult();
	}






	public boolean find()
	{

		boolean b= m.find();

		values=new Object[m.groupCount()+1];
		return b;
	}

	public String group()
	{
		return m.group();
	}
	public String group(int group)
	{
		return m.group(group);
	}

	public void replace(Object[] obj)
	{
		// System.out.println(format);

		replace( fmt.format(obj));//
	}

	public void replace(String s)
	{

		//m.appendReplacement(buf, s);//
		buf.append( content.substring(pointer,m.start()));
		buf.append(s );
		pointer=m.end();


	}



	public int groupCount()
	{
		return m.groupCount();
	}

	/**
	 * 设置替换的新值
	 * @param i
	 * @param o
	 */
	public void setGroup(int i ,Object o)
	{
		values[i]=o;
	}

	/**
	 * 设置整个匹配项
	 * @param o
	 */
	public void setGroup(Object o)
	{
		values[0]=o;
	}

	public void replace()
	{
		//先排除重叠的group
		for(int i=1;i< values.length;i++)
		{
			if(values[i]==null) continue;
			int start=m.start(i);
			int end=m.end(i);
			for(int k=0;k<i;k++)
			{
				if(values[k]==null) continue;
				if(start>=m.start(k)&& start<m.end(k))
				{
					values[i]=null; //设置为空，则不管这个group
					//System.out.println("values[i]"+i);
					break;
				}
			}
		}


		for(int i=0;i< values.length;i++)
		{
			if(values[i]==null) continue;
			int start=m.start(i);
			int end=m.end(i);
			//System.out.println("pointer="+pointer+"start="+start+"end="+end);
			buf.append( content.substring(pointer,start));
			buf.append(values[i] );
			pointer=end;

		}


	}


	public String getResult()
	{
		if(!b)
		{
			//m.appendTail(buf);
			buf.append(content.substring(pointer));
			b=true;
		}
		return buf.toString();
	}

	public static void main(String[] args)
	{
		//-- 全部替换为一常量 ，和String.replaceAll()一致
		RegularReplace rr=new RegularReplace("a1b2c3d3e3","[^c]3");
		System.out.println(rr.replaceAll("-"));

		//-- 全部替换某一组为一常量 (注意 group=0表示整个匹配串，从group=1开始才是真正的组
		rr=new RegularReplace("*abc*abc*","a(b)(c)");
		System.out.println(rr.replaceAll(1,"-"));

		//-- 全部替换为动态值
		rr=new RegularReplace("*abc*abc*","a(b)(c)");
		int n=0;
		while(rr.find())
		{
			rr.replace("ME"+n);

			n++;

		}

		System.out.println(rr.getResult());

		//-- 全部替换某几个组为动态值
		rr=new RegularReplace("*abc*abc*","a(b)(c)");
		while(rr.find())
		{

			rr.setGroup(1, "B"+n);
			rr.setGroup(2, "C"+n);
			rr.replace();
			n++;

		}
		System.out.println(rr.getResult());


	}


}
