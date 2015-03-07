package agent;

import java.lang.instrument.Instrumentation;

/**
 * javaagent 测试类，简单的打印系统加载的类
 * Created by addison.li on 2015/3/7.
 */
public class CheckLoadClassAgent {

    public static void premain(String args, Instrumentation instrumentation){
        instrumentation.addTransformer(new ClassLoadTransformer());

    }
}
