public class ClassTest {

    int x, y;

    ClassTest(int a, int b) throws RuntimeException {
        try {
            x = a;
            y = b;
            throw new RuntimeException();
        } catch (RuntimeException e) {
            e.getMessage();
        }
    }

    public int getX() throws RuntimeException {
        try {
            System.out.println("if");
            throw new RuntimeException();
        } catch (RuntimeException e) {
            System.out.println("Error");
        }
        return x;
    }

    public int getY() throws RuntimeException {
        try {
            System.out.println("Y");
        } catch (RuntimeException e) {
            // No handling
        }
        return y;
    }

    public void setX(int x) throws Exception{
        try {
            if(x==0) throw new Exception("error");
        }catch (Exception e){
            // No handling
        }
    }
}
