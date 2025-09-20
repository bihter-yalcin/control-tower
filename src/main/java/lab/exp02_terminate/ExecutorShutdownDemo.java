package lab.exp02_terminate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorShutdownDemo {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService pool = Executors.newFixedThreadPool(2);

        for (int i = 1; i <= 5; i++) {
            int passengerId = i;
            pool.submit(() -> {
                String t = Thread.currentThread().getName();
                System.out.println("Gate serving passenger " + passengerId + " on " + t);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                System.out.println("Passenger " + passengerId + " done on " + t);
            });
        }

        //Add shutdownNow() to see forceful termination.
        pool.shutdown();

        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
            System.out.println("Some tasks did not finish in time!");
        } else {
            System.out.println("All passengers processed, executor terminated.");
        }
    }
}
