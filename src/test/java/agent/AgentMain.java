package agent;

import com.addison.framework.agent.CollectionAgent;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * Created by addison.li on 2015/3/7.
 */
public class AgentMain {


    public static void main(String[] args){
//        testCollection();
        testObjectSize();



    }


    public static void testObjectSize(){
        System.out.println("sizeOf(new A())=" + CollectionAgent.sizeOf(new A()));
        try {
            System.out.println("sizeOf(new String())=" + CollectionAgent.sizeOf(new String("")));
            System.out.println("fullSizeOf(new String())=" + CollectionAgent.fullSizeOf(new String("")));
            System.out.println("fullSizeOf(new String(a))=" + CollectionAgent.fullSizeOf(new String("a")));
            System.out.println("fullSizeOf(new String(aa))=" + CollectionAgent.fullSizeOf(new String("aa")));
            System.out.println("fullSizeOf(new String(aaa))=" + CollectionAgent.fullSizeOf(new String("aaa")));
            System.out.println("fullSizeOf(new String(aaaa))=" + CollectionAgent.fullSizeOf(new String("aaaa")));
            System.out.println("fullSizeOf(new String(aaaaa))=" + CollectionAgent.fullSizeOf(new String("aaaaaa")));
            System.out.println("fullSizeOf(new String(aaaaaa))=" + CollectionAgent.fullSizeOf(new String("aaaaaaa")));
            System.out.println("sizeOf(new Integer(0))=" + CollectionAgent.sizeOf(new Integer[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void testCollection(){
        /**
         * 由于ConcurrentHashMap 没有继承hashmap 所以不对ConcurrentHashMap生效
         */
        Map<String,String> map  = new ConcurrentHashMap<String, String>();
        for(int n =1;n<1002;n++){
            System.out.println(n);
            map.put(String.valueOf(n), String.valueOf(n));
        }

        Map<String,String> map1  = new HashMap<String, String>();
        for(int n =1;n<1002;n++){
            System.out.println(n);
            map.put(String.valueOf(n), String.valueOf(n));
        }
    }
}
