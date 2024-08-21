import org.example.MyClassKotlin;

public class MyClassJava {

    public static void main(String[] args) {

        MyClassKotlin.myStaticFunction();
    }

    public void runCode(){

        MyClassKotlin myClassKotlin = new MyClassKotlin();

        int value = myClassKotlin.myPublicField;

        myClassKotlin.doSomething(1);

        myClassKotlin.myJavaName();
    }
}
