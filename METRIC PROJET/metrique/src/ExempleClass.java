public class ExempleClass {

    class Vehicule{
        String owner;

        public void sound(){
            System.out.println("vehicule sound.");
        }

        public void move(){
            System.out.println("vehicule moving.");
        }
    }

    class Car extends Vehicule{
        public Car(String owner){
            this.owner = owner;
        }

        @Override
        public void sound(){
            System.out.println("car horn");
        }
    }
    public static void main(String[] args) {
        Car car = new ExempleClass().new Car("someone");
        Car car2 = new ExempleClass().new Car("someone else");
        Car car3 = new ExempleClass().new Car("another one");
        car.sound();
        car.sound();
        car2.sound();
        car2.sound();
        car.move();
        car2.move();
        car3.move();
        car3.move();
        car3.move();
        car.move();
    
    }
}
