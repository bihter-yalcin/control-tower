package lab.exp07_interThreadCommunication;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class InterThreadCommunication {

    static final class Aircraft {
        final String callSign;
        final boolean runwayClosedSignal;

        private Aircraft(String callSign, boolean runwaySignal) {
            this.callSign = callSign;
            this.runwayClosedSignal = runwaySignal;
        }

        static Aircraft plane(String callSign) {
            return new Aircraft(callSign, false);
        }

        static Aircraft runwayClosed() {
            return new Aircraft("RUNWAY_CLOSED", true);
        }

        @Override
        public String toString() {
            return runwayClosedSignal ? "RUNWAY_CLOSED" : callSign;
        }

    }

    static final class RunwayBuffer {
        private Aircraft slot;

        public synchronized void put(Aircraft a) throws InterruptedException {
            while (slot != null) {
                log("Runway full. Tower waits…");
                wait();
            }
            slot = a;
            log("Runway RECEIVED -> " + a);
            notifyAll();
        }

        public synchronized Aircraft take() throws InterruptedException {
            while (slot == null) {
                log("Runway empty. Pilot waits…");
                wait();
            }
            Aircraft a = slot;
            slot = null;
            log("Runway RELEASED <- " + a);
            notifyAll();
            return a;
        }
    }

    static final class ControlTowerProducer implements Runnable {

        private final RunwayBuffer runway;
        private final int totalPlanes;
        private final int consumerCount;

        ControlTowerProducer(RunwayBuffer runway, int totalPlanes, int consumerCount) {
            this.runway = runway;
            this.totalPlanes = totalPlanes;
            this.consumerCount = consumerCount;
        }


        @Override
        public void run() {
            try {
                for (int i = 1; i <= totalPlanes; i++) {
                    Aircraft a = Aircraft.plane("TK" + String.format("%03d", i));
                    log("Tower preparing -> " + a);
                    runway.put(a);
                    sleep(120, 320);
                }
                for (int i = 0; i < consumerCount; i++) {
                    Aircraft closed = Aircraft.runwayClosed();
                    log("Tower broadcasting closure -> " + closed);
                    runway.put(closed);
                }
                log("Tower done.");
            } catch (InterruptedException e) {
                log("Tower interrupted. Shutting down.");
                Thread.currentThread().interrupt();
            }

        }
    }

    static final class PilotConsumer implements Runnable {
        private final RunwayBuffer runway;
        private final String name;

        PilotConsumer(RunwayBuffer runway, String name) {
            this.runway = runway;
            this.name = name;
        }

        @Override public void run() {
            try {
                while (true) {
                    Aircraft a = runway.take();
                    if (a.runwayClosedSignal) {
                        log(name + " sees RUNWAY_CLOSED. Taxi to hangar & exit.");
                        break;
                    }
                    log(name + " taxiing & takeoff: " + a);
                    sleep(200, 500);
                    log(name + " completed flight: " + a);
                }
            } catch (InterruptedException e) {
                log(name + " interrupted. Returning to base.");
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RunwayBuffer runway = new RunwayBuffer();
        int consumerCount = 2;
        int totalPlanes   = 8;

        Thread tower  = new Thread(new ControlTowerProducer(runway, totalPlanes, consumerCount), "ControlTower");
        Thread pilot1 = new Thread(new PilotConsumer(runway, "Pilot-1"), "Pilot-1");
        Thread pilot2 = new Thread(new PilotConsumer(runway, "Pilot-2"), "Pilot-2");

        tower.start();
        pilot1.start();
        pilot2.start();

        tower.join();
        pilot1.join();
        pilot2.join();

        log("Simulation finished.");
    }

    static void sleep(int minMs, int maxMs) throws InterruptedException {
        int dur = minMs + (int)(Math.random() * (maxMs - minMs + 1));
        Thread.sleep(dur);
    }

    static final DateTimeFormatter F = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    static void log(String msg) {
        System.out.printf("[%s] %s%n", LocalTime.now().format(F), msg);
    }

}
