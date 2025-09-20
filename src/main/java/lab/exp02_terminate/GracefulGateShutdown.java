package lab.exp02_terminate;

public class GracefulGateShutdown {

    static class GateWorker implements Runnable {
        private volatile boolean running = true;
        private int served = 0;

        public void stop() {
            running = false;
        }

        public int servedCount() {
            return served;
        }

        @Override
        public void run() {

            final String thread = Thread.currentThread().getName();
            System.out.println("Gate A opened by " + thread);

            while (running && !Thread.currentThread().isInterrupted()) {
                served++;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Gate A interrupted; prepared for close !!");
                }
            }
            System.out.println("Gate A closed. Served=" + served);

        }
    }

    public static void main(String[] args) throws InterruptedException {
        GateWorker worker = new GateWorker();
        Thread thread = new Thread(worker,"gate-A-worker");

        thread.start();
        Thread.sleep(2000);

        System.out.println("Control Tower; closing Gate A (graceful)");
        worker.stop();
       // thread.interrupt();

        thread.join();
        System.out.println("Graceful shutdown complete. Total served = " + worker.servedCount());
    }
}
