package com.addison.framework.agent;

import javassist.ClassPool;
import javassist.CtClass;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;

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



  public static void premain(String args, Instrumentation instrumentation) {
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




}