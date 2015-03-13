package agent;

import java.io.Serializable;

/**
 * Created by addison.li on 2015/3/8.
 */
public class Student implements Serializable{
    private  String name;//
    private static int age;//

    public String getInfo(){
        return "student,name:"+this.name+",age:"+this.age;
    }

    public  String getName() {
        return name;
    }

    public  void setName(String name) {
        this.name = name;
    }

    public static int getAge() {
        return age;
    }

    public static void setAge(int age) {
        Student.age = age;
    }
}
