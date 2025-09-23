package lab.exp06_locking;

public class RunwaySynchronized {
    static class Runway {
        public synchronized void use(String flight, long ms) {
            String t = Thread.currentThread().getName();
            System.out.println(flight + " acquired runway on " + t);
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                System.out.println(flight + " released runway on " + t);
            }
        }
    }

    static class FlightTask implements Runnable {
        private final Runway runway;
        private final String flight;
        private final long workMs;

        FlightTask(Runway runway, String flight, long workMs) {
            this.flight = flight;
            this.runway = runway;
            this.workMs = workMs;

        }

        @Override
        public void run() {
            runway.use(flight, workMs);
        }

        public static void main(String[] args) throws InterruptedException {
            Runway runway = new Runway();

            Thread f1 = new Thread(new FlightTask(runway, "TK101", 1200), "tower-1");
            Thread f2 = new Thread(new FlightTask(runway, "TK202", 1200), "tower-2");
            Thread f3 = new Thread(new FlightTask(runway, "TK303", 1200), "tower-3");
            f1.start();
            f2.start();
            f3.start();

            f1.join();
            f2.join();
            f3.join();

            System.out.println("All flights finished - runway is empty right now");
        }

    }
}
