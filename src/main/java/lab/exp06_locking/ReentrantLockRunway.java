package lab.exp06_locking;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantLockRunway {

    static class Resource {
        final String name;
        final ReentrantLock lock;
        Resource(String name, boolean fair) {
            this.name = name;
            this.lock = new ReentrantLock(fair);
        }
    }

    static class Flight implements Runnable {
        private final String flight;
        private final Resource tractor;
        private final Resource runway;

        Flight(String flight, Resource tractor, Resource runway) {
            this.flight = flight; this.tractor = tractor; this.runway = runway;
        }

        @Override public void run() {
            int attempts = 0;
            while (attempts++ < 5) {
                boolean gotTractor = false, gotRunway = false;
                try {
                    gotTractor = tractor.lock.tryLock(500, TimeUnit.MILLISECONDS);
                    if (!gotTractor) {
                        System.out.println(flight + " failed to get " + tractor.name);
                        backoff(attempts);
                        continue;
                    }
                    System.out.println(flight + " got " + tractor.name);

                    gotRunway = runway.lock.tryLock(500, TimeUnit.MILLISECONDS);
                    if (!gotRunway) {
                        System.out.println(flight + " failed to get " + runway.name + " (releasing tractor)");
                        backoff(attempts);
                        continue;
                    }
                    System.out.println(flight + " got " + runway.name);

                    Thread.sleep(400);
                    System.out.println(flight + " done!");
                    return;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    if (gotRunway && runway.lock.isHeldByCurrentThread()) runway.lock.unlock();
                    if (gotTractor && tractor.lock.isHeldByCurrentThread()) tractor.lock.unlock();
                }
            }
            System.out.println(flight + " gave up after retries.");
        }

        static void backoff(int attempts) {
            try { Thread.sleep(100 + attempts * 50L); } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Resource tractor = new Resource("TRACTOR", true);
        Resource runway  = new Resource("RUNWAY",  true);

        Thread a = new Thread(new Flight("TK701", tractor, runway));
        Thread b = new Thread(new Flight("TK702", tractor, runway));
        a.start(); b.start();
        a.join();  b.join();
        System.out.println("Finished with ReentrantLock — no deadlock.");
    }
}