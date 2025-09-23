package lab.exp06_locking;

public class Deadlock {

    static final Object TRACTOR = new Object();
    static final Object RUNWAY = new Object();

    static class FlightA implements Runnable {
        @Override
        public void run() {
            synchronized (TRACTOR) {
                System.out.println("A got TRACTOR");
                sleep(300);
                synchronized (RUNWAY) {
                    System.out.println("A got RUNWAY");
                }
            }
        }
    }

    static class FlightB implements Runnable {
        @Override
        public void run() {
            synchronized (RUNWAY) {
                System.out.println("B got RUNWAY");
                sleep(300);
                synchronized (TRACTOR) {
                    System.out.println("B got TRACTOR");
                }
            }
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {

        new Thread(new FlightA(), "flight-A").start();
        new Thread(new FlightB(), "flight-B").start();

    }
}
