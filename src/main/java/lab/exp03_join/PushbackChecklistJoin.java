package lab.exp03_join;


public class PushbackChecklistJoin {

    static class PreperationTask implements Runnable {
        private final String name;
        private final long workMs;

        PreperationTask(String name, long workMs) {
            this.workMs = workMs;
            this.name = name;
        }


        @Override
        public void run() {
            String thread = Thread.currentThread().getName();
            System.out.println(name + " started on " + thread);

            try {
                Thread.sleep(workMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(name + " interrupted on " + thread);
                return;
            }
            System.out.println(name + " completed on " + thread + " (" + workMs + " ms)");
        }


    }

    public static void main(String[] args) throws InterruptedException {
        Thread catering = new Thread(new PreperationTask("Catering", 1500), "catering-thread");
        Thread fueling = new Thread(new PreperationTask("Fueling", 3000), "catering-thread");
        Thread boarding = new Thread(new PreperationTask("Boarding", 2000), "catering-thread");

        long start = System.nanoTime();

        catering.start();
        fueling.start();
        boarding.start();

        catering.join();
        fueling.join();
        boarding.join();

        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.println("All preparations completed in " + elapsedMs + " ms → Ready for pushback");
    }
}
