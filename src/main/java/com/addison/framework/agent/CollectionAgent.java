package com.addison.framework.agent;

import javassist.ClassPool;
import javassist.CtClass;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 *
 */
public abstract class CollectionAgent {

  /**
   * agent传递的参数
   */
  private static final String PARAM_SIZE = "size";

  //集合限制大小
  private static long size = 100000;

  static Instrumentation inst;


  public static void premain(String args, Instrumentation instrumentation) {
    inst = instrumentation;
    //解析参数
    if(args != null && args.trim().length() > 0) {
      try{
        String[] params = args.trim().split(",");
        if(params != null && params.length > 0) {
          for (String param : params) {
            if(param != null && param.trim().length() > 0) {
              String[] paramPair = param.split("=");
              if (paramPair != null && paramPair.length == 2) {
                if(paramPair[0].trim().equals(PARAM_SIZE)) {
                  size = Long.parseLong(paramPair[1].trim());
                }
              }
            }
          }
        }
      } catch (Throwable throwable) {
        throw new IllegalArgumentException("无效的参数：" + args);
      }
    }
    ClassPool classPool = ClassPool.getDefault();
    try {
      //javasist的类型
      CtClass integer = classPool.get("int");
      CtClass floatClass = classPool.get("float");
      CtClass object = classPool.get("java.lang.Object");
      CtClass collection = classPool.get("java.util.Collection");
      CtClass arrayList = classPool.get("java.util.ArrayList");
      CtClass map = classPool.get("java.util.Map");
      CtClass hashMap = classPool.get("java.util.HashMap");

      //集合异常时抛出异常的源码
      String throwSource = "throw new RuntimeException(\"超出了\" + this.getClass().getName() + \"的最大容量:\" + " + size + ");";

      //源码
      String listInitSource = "if(elementData.length > " + size + ") { " + throwSource + " } ";
      String addSource = "if(size() > " + size + "){" + throwSource + "}";
      //改写ArrayList(int)
      arrayList.getDeclaredConstructor(new CtClass[]{integer}).insertAfter(listInitSource);
      //ArrayList(Collection<? extends E> c)
      arrayList.getDeclaredConstructor(new CtClass[]{collection}).insertAfter(addSource);

      //add(E)
      arrayList.getDeclaredMethod("add", new CtClass[]{object}).insertAfter(addSource);
      //addAll(int, E)
      arrayList.getDeclaredMethod("add", new CtClass[]{integer, object}).insertAfter(addSource);
      //addAll(Collection)
      arrayList.getDeclaredMethod("addAll", new CtClass[]{collection}).insertAfter(addSource);
      //add(int, Collection)
      arrayList.getDeclaredMethod("addAll", new CtClass[]{integer, collection}).insertAfter(addSource);

      //init source
      String mapInitSource = "if(table.length > " + size + ") { " + throwSource + "}";
      //HashMap(int initialCapacity, float loadFactor),
      hashMap.getDeclaredConstructor(new CtClass[]{integer, floatClass}).insertAfter(mapInitSource);
      //put source
      String putSource = "if(size() > " + size + ") {" + throwSource + "}";
      //HashMap(Map<? extends K, ? extends V> m)
      hashMap.getDeclaredConstructor(new CtClass[]{map}).insertAfter(putSource);
      //put(K key, V value)
      hashMap.getDeclaredMethod("put", new CtClass[]{object, object}).insertAfter(putSource);
      //putAll(Map<? extends K, ? extends V> m)
      hashMap.getDeclaredMethod("putAll", new CtClass[]{map}).insertAfter(putSource);

      //重新生成字节码

     instrumentation.redefineClasses(new ClassDefinition[]{
              new ClassDefinition(ArrayList.class, arrayList.toBytecode()),
              new ClassDefinition(HashMap.class, hashMap.toBytecode())});
    } catch (Throwable throwable) {
      throw new RuntimeException("创建集合代理出错", throwable);
    }
  }

  /**
   * 直接计算当前对象占用空间大小，包括当前类及超类的基本类型实例字段大小、<br></br>
   * 引用类型实例字段引用大小、实例基本类型数组总占用空间、实例引用类型数组引用本身占用空间大小;<br></br>
   * 但是不包括超类继承下来的和当前类声明的实例引用字段的对象本身的大小、实例引用数组引用的对象本身的大小 <br></br>
   *
   * @param obj
   * @return
   */
  public static long sizeOf(Object obj) {
    return inst.getObjectSize(obj);
  }

  /**
   * 递归计算当前对象占用空间总大小，包括当前类和超类的实例字段大小以及实例字段引用对象大小
   *
   * @param objP
   * @return
   * @throws IllegalAccessException
   */
  public static long fullSizeOf(Object objP) throws IllegalAccessException {
    Set<Object> visited = new HashSet<Object>();
    Deque<Object> toBeQueue = new ArrayDeque<Object>();
    toBeQueue.add(objP);
    long size = 0L;
    while (toBeQueue.size() > 0) {
      Object obj = toBeQueue.poll();
      //sizeOf的时候已经计基本类型和引用的长度，包括数组
      size += skipObject(visited, obj) ? 0L : sizeOf(obj);
      Class<?> tmpObjClass = obj.getClass();
      if (tmpObjClass.isArray()) {
        //[I , [F 基本类型名字长度是2
        if (tmpObjClass.getName().length() > 2) {
          for (int i = 0, len = Array.getLength(obj); i < len; i++) {
            Object tmp = Array.get(obj, i);
            if (tmp != null) {
              //非基本类型需要深度遍历其对象
              toBeQueue.add(Array.get(obj, i));
            }
          }
        }
      } else {
        while (tmpObjClass != null) {
          Field[] fields = tmpObjClass.getDeclaredFields();
          for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())   //静态不计
                    || field.getType().isPrimitive()) {    //基本类型不重复计
              continue;
            }

            field.setAccessible(true);
            Object fieldValue = field.get(obj);
            if (fieldValue == null) {
              continue;
            }
            toBeQueue.add(fieldValue);
          }
          tmpObjClass = tmpObjClass.getSuperclass();
        }
      }
    }
    return size;
  }

  /**
   * String.intern的对象不计；计算过的不计，也避免死循环
   *
   * @param visited
   * @param obj
   * @return
   */
  static boolean skipObject(Set<Object> visited, Object obj) {
    if (obj instanceof String && obj == ((String) obj).intern()) {
      return true;
    }
    return visited.contains(obj);
  }



}