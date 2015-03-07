# collection-agent
通过javaagent和javassist技术实现对java的ArrayList和HashMap的增强，避免在虚拟器一次load大量数据时导致OOM.
虚拟机通过配置参数：
-javaagent:d:/test/collection-agent-1.0.0-SNAPSHOT.jar=size=100000
size 限制的集合大小

