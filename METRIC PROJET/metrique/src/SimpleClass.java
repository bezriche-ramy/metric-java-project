// SimpleClassWithoutDependencies.java
public class SimpleClass {
    public int x;

    public SimpleClass(int x) {
        this.x = x;
    }

    public void increment() {
        x++;
    }
    public static void main(String[] args) {
        SimpleClass y = new SimpleClass(5);
        System.out.println(y);
    }
}