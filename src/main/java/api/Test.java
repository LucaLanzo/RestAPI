package api;


import com.owlike.genson.Genson;
import resources.Course;

/***
 * By Luca Lanzo
 */


public class Test {
    public static void main(String[] args) {
        Genson builder = new Genson();
        Course course = new Course("TestKurs");
        System.out.println(builder.serialize(course));
    }
}
