package lab.exp05_producerConsumer;

import java.util.Random;
import java.util.concurrent.*;

public class BaggageBeltSystem {

    //There are 10 Baggage Belt
    private static final BlockingQueue<String> belt = new ArrayBlockingQueue<>(10);


    //Passengers are producers. They leave baggage to belt.
    static class Passenger implements Runnable {

        private final int id;
        private final Random rnd = new Random();

        Passenger(int id) {
            this.id = id;
        }

        @Override
        public void run() {

            try {
                String bag = "Bag-" + id;
                System.out.println("Passenger " + id + " dropping " + bag);
                belt.put(bag);
                Thread.sleep(200 + rnd.nextInt(300));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }

    //Staff handles the baggage.
    static class Handler implements Runnable {
        private final int id;

        Handler(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String bag = belt.take();
                    System.out.println("Handler " + id + " loading " + bag);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Handler " + id + " interrupted, giving a break.");
                Thread.currentThread().interrupt();
            }
        }

        public static void main(String[] args) throws InterruptedException {
            ExecutorService passengers = Executors.newFixedThreadPool(5);
            ExecutorService handlers = Executors.newFixedThreadPool(2);

            //20 passengers will leave baggage
            for (int i = 0; i <= 20; i++) {
                passengers.submit(new Passenger(i));
            }

            // 2 staff is working.
            for (int i = 1; i <= 2; i++) {
                handlers.submit(new Handler(i));
            }

            passengers.shutdown();
            passengers.awaitTermination(10, TimeUnit.SECONDS);

            Thread.sleep(2000); // biraz daha bagaj alsınlar
            handlers.shutdownNow();

        }
    }
}
