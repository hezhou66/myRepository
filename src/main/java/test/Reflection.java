package test;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Reflection {
	
	public static String s = "NIHAO";
	
	public Reflection() {
		s = "NIMEI";
	}
	
	public Object getProperty(Object owner,String fieldName) throws Exception
	{
		Class ownerClass = owner.getClass();
		Field field = ownerClass.getField(fieldName);
		Object property = field.get(owner);
		return property;
	}
	
	public Object getStaticProperty(String className,String fieldName) throws Exception
	{
		Class ownerClass = Class.forName(className);
		Field field = ownerClass.getField(fieldName);
		Object property = field.get(ownerClass);
		return property;
	}
	
	public Object invokeMethod(Object owner,String methodName,Object[] args) throws Exception
	{
		Class ownerClass = owner.getClass();
		Class[] argClass = new Class[args.length];
		for(int i =0;i<args.length;i++)
		{
			argClass[i]=args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argClass);
		Object result = method.invoke(owner, args);
		return result;
	}
	
	public Object invokeStaticMethod(String className,String methodName,Object[] args) throws Exception
	{
		Class ownerClass = Class.forName(className);
		Class[] argClass = new Class[args.length];
		for(int i =0;i<args.length;i++)
		{
			argClass[i]=args[i].getClass();
		}
		Method method = ownerClass.getMethod(methodName, argClass);
		Object result = method.invoke(null, args);
		return result;
	}
	
	public static Object newInstance(String className,Object[] args) throws Exception
	{
		Class newoneClass = Class.forName(className);
		Class[] argsClass = new Class[args.length];
		for(int i=0;i<args.length;i++)
		{
			argsClass[i]=args[i].getClass();
		}
		Constructor cons = newoneClass.getConstructor(argsClass);
		return cons.newInstance(args);
	}
	
	public Object getByArray(Object array,int index)
	{
		return Array.get(array, index);
	}
	
	public static String gets()
	{
		return s;
	}
	
	public static void main(String[] args) throws Exception {
		Reflection re = (Reflection) Reflection.newInstance(Reflection.class.getName(), new Object[]{});
		//re.s="HEHE";
		String he = (String) re.getProperty(re, "s");
		System.out.println(he);
		he = (String) re.invokeMethod(re, "getStaticProperty", new Object[]{re.getClass().getName(),"s"});
		System.out.println(he);
		he = (String) re.invokeStaticMethod(re.getClass().getName(), "gets", new Object[]{});
		System.out.println(he);
	}

}
