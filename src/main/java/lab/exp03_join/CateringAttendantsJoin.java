package lab.exp03_join;

public class CateringAttendantsJoin {

    static class CateringTask implements Runnable {

        private final int trolleyId;
        private final long workMs;

        CateringTask(int trolleyId, long workMs) {
            this.trolleyId = trolleyId;
            this.workMs = workMs;
        }

        @Override
        public void run() {
            String thread = Thread.currentThread().getName();
            System.out.println("Catering trolley " + trolleyId + " handled by " + thread);

            try {
                Thread.sleep(workMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.out.println("Catering trolley " + trolleyId + " completed by " + thread);
        }
    }

    private static long runWithOneAttendant(int trolleys) throws InterruptedException {
        long start = System.nanoTime();
        for (int i = 0; i <= trolleys; i++) {
            Thread thread = new Thread(new CateringTask(i, 500), "attendant-1");
            thread.start();

            //Wait current thread to finish.
            thread.join();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static long runWithTwoAttendant(int trolleys) throws InterruptedException {
        long start = System.nanoTime();
        Thread[] threads = new Thread[trolleys];

        for (int i = 1; i <= trolleys; i++) {
            threads[i - 1] = new Thread(new CateringTask(i, 500), "attendant-" + ((i % 2)));
            threads[i - 1].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        return (System.nanoTime() - start) / 1_000_000;

    }

    public static void main(String[] args) throws InterruptedException {
        int trolleys = 8;

        System.out.println("Catering with 1 attendant");
        long ms1 = runWithOneAttendant(trolleys);
        System.out.println("Total time " + ms1);

        System.out.println("Catering with 2 attendant");
        long ms2 = runWithTwoAttendant(trolleys);
        System.out.println("Total time " + ms2);


    }

}

