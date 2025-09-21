package lab.exp04_latency;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SecurityLatencyTuning {
    static long sampleServiceTimeMillis(java.util.Random r) {
        long t = 120 + r.nextInt(101);
        if (r.nextDouble() < 0.07)
            t += 800 + r.nextInt(701);
        return t;
    }

    static double percentile(List<Long> sortedMillis, double p) {
        if (sortedMillis.isEmpty()) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sortedMillis.size()) - 1;
        idx = Math.max(0, Math.min(idx, sortedMillis.size() - 1));
        return sortedMillis.get(idx);
    }


    static RunResult runOnce(int passengers, int lanes, long seed) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(lanes, r -> {
            Thread t = new Thread(r);
            t.setName("lane-" + t.getId());
            return t;
        });

        List<Long> latencies = Collections.synchronizedList(new ArrayList<>(passengers));
        Random rnd = new Random(seed);
        CountDownLatch done = new CountDownLatch(passengers);

        long wallStart = System.nanoTime();

        for (int i = 0; i < passengers; i++) {
            pool.submit(() -> {
                long start = System.nanoTime();
                try {
                    Thread.sleep(sampleServiceTimeMillis(rnd));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    long durMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
                    latencies.add(durMs);
                    done.countDown();
                }
            });
        }

        done.await();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        long wallMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - wallStart);

        // Compute stats from per-passenger latencies
        List<Long> sorted = latencies.stream().sorted().collect(Collectors.toList());
        long min = sorted.isEmpty() ? 0 : sorted.get(0);
        long max = sorted.isEmpty() ? 0 : sorted.get(sorted.size() - 1);
        double p50 = percentile(sorted, 50);
        double p95 = percentile(sorted, 95);
        double p99 = percentile(sorted, 99);
        double avg = sorted.stream().mapToLong(Long::longValue).average().orElse(0);
        double thr = passengers * 1000.0 / Math.max(1, wallMs); // passenger per second

        return new RunResult(passengers, lanes, wallMs, min, max, p50, p95, p99, avg, thr);
    }


    public static void main(String[] args) throws InterruptedException {
        int passengers = 300;

        System.out.println("== Security Screening Latency Tuning ==");
        System.out.println("Passengers=" + passengers + " (random mix with ~7% long tails)");

        runOnce(50, 2, 42);

        RunResult r1 = runOnce(passengers, 1, 123);
        r1.print();

        RunResult r2 = runOnce(passengers, 4, 123);
        r2.print();

        RunResult r3 = runOnce(passengers, 8, 123);
        r3.print();

        System.out.println();
        System.out.println("Notes:");
        System.out.println("- More lanes usually reduce Wall and increase throughput.");
        System.out.println("- p50 often stays similar (single-passenger service time).");
        System.out.println("- p95/p99 (tail) can improve as queues shrink, but tails never fully vanish (slow passengers remain slow).");
        System.out.println("- Too many lanes can introduce overhead (context switching/CPU contention) → diminishing returns.");
    }
}




