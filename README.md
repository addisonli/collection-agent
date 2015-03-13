# collection-agent
通过javaagent和javassist技术实现对java的ArrayList和HashMap的增强，在操作集合元素时判断集合元素个数，
当集合元素个数大于设置的上限时，抛出异常，终止此次操作，从而避免在集合元素过大导致OOM.

#使用方式：
-javaagent:d:/test/collection-agent-1.0.0-SNAPSHOT.jar=size=100000
size 限制的集合大小

#另附
javaAgent是从JDK1.5及以后引入的，在1.5之前无法使用，也可以叫做java代理。
代理 (agent) 是在main方法前的一个拦截器 (interceptor)，也就是在main方法执行之前，执行agent的代码。
agent的代码与你的main方法在同一个JVM中运行，并被同一个system classloader装载，被同一的安全策略 (security policy) 和上下文 (context) 所管理。

