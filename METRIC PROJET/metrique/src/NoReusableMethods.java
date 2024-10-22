public class NoReusableMethods {
    public void method1() {
        int x = 10;
        int y = 20;
        int sum = x + y;
        System.out.println("Sum: " + sum);
    }

    public void method2() {
        System.out.println("Hello, World!");
    }
}