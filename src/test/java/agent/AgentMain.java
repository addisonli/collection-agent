package agent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by addison.li on 2015/3/7.
 */
public class AgentMain {

    public static void main(String[] args){
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
        System.out.print("test finish.");


    }
}
