package lab.exp8_async;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class AirportAsync {
    private static final ExecutorService pool = Executors.newFixedThreadPool(3);

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }


    static CompletableFuture<String> fueling() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Fueling started...");
            sleep(1000);
            System.out.println("Fueling done.");
            return "FUEL_OK";
        }, pool);
    }

    static CompletableFuture<String> catering() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Catering started...");
            sleep(1200);
            System.out.println("Catering done.");
            return "CATERING_OK";
        }, pool);
    }

    static CompletableFuture<String> security() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Security check started...");
            sleep(800);
            System.out.println("Security check done.");
            return "SECURITY_OK";
        }, pool);
    }

    static CompletableFuture<String> pushback() {
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Pushback started...");
            sleep(500);
            System.out.println("Pushback done. Ready for taxi!");
            return "READY_FOR_TAKEOFF";
        }, pool);
    }

    public static void main(String[] args) throws Exception {
        CompletableFuture<String> fuel =
                fueling().orTimeout(1500, TimeUnit.MILLISECONDS);

        Supplier<CompletableFuture<String>> securityAttempt =
                () -> security().orTimeout(1000, TimeUnit.MILLISECONDS);

        CompletableFuture<String> securityF =
                //max retries and initial Backoff
                retryAsync(securityAttempt, 2, 300);

        CompletableFuture<String> cateringF =
                catering()
                        .orTimeout(1100, TimeUnit.MILLISECONDS)
                        .exceptionally(ex -> {
                            System.out.println("Catering failed/timeout → FALLBACK_MENU (" + ex.getClass().getSimpleName() + ")");
                            return "FALLBACK_MENU";
                        });


        CompletableFuture<String> fuelSafe = fuel.handle((ok, ex) -> ex == null ? ok : "FAIL");
        CompletableFuture<String> securitySafe = securityF.handle((ok, ex) -> ex == null ? ok : "FAIL");


        CompletableFuture<Void> all = CompletableFuture.allOf(fuelSafe, cateringF, securitySafe);

        CompletableFuture<String> result = all.thenCompose(v -> {
            String f = fuelSafe.join();
            String c = cateringF.join();
            String s = securitySafe.join();

            System.out.println("Services → fuel=" + f + ", catering=" + c + ", security=" + s);

            boolean criticalOk = "FUEL_OK".equals(f) && "SECURITY_OK".equals(s);
            if (!criticalOk) {
                System.out.println("Critical service failed → Departure ABORTED");
                return CompletableFuture.completedFuture("ABORTED");
            }
            return pushback();
        });


        try {
            String outcome = result.get(5, TimeUnit.SECONDS);
            System.out.println("Final outcome: " + outcome);
        } finally {
            pool.shutdown();
        }
    }

    // This is retry helper class
    static <T> CompletableFuture<T> retryAsync(
            Supplier<CompletableFuture<T>> attempt,
            int maxRetries, long initialBackoffMillis) {

        CompletableFuture<T> result = new CompletableFuture<>();
        AtomicInteger tries = new AtomicInteger(0);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                int n = tries.getAndIncrement();
                attempt.get().whenComplete((val, err) -> {
                    if (err == null) {
                        result.complete(val);
                    } else if (n < maxRetries) {
                        long delay = (long) (initialBackoffMillis * Math.pow(2, n));
                        System.out.println("Retrying security in " + delay + " ms...");
                        scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
                    } else {
                        result.completeExceptionally(err);
                    }
                });
            }
        };
        run.run();
        return result;
    }

}

