package lab.exp06_locking;

public class OrderedLock {
    static final Object TRACTOR = new Object();
    static final Object RUNWAY = new Object();

    static class Flight implements Runnable {
        private final String name;

        Flight(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            synchronized (TRACTOR) {
                System.out.println(name + " got a TRACTOR");
                sleep(200);
                synchronized (RUNWAY) {
                    System.out.println(name + "  got RUNWAY");
                    sleep(200);
                }
            }
            System.out.println(name + " released both ");
        }

        static void sleep(long ms) {
            try {
                Thread.sleep(ms);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        public static void main(String[] args) throws InterruptedException {
            Thread a = new Thread(new Flight( "TK101"));
            Thread b = new Thread(new Flight( "TK202"));

            a.start();
            b.start();
            a.join();
            b.join();

            System.out.println("Finished - no deadlock");
        }
    }
}
