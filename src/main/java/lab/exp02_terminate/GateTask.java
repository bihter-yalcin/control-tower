package lab.exp02_terminate;

public class GateTask implements Runnable {

    private final int passengerId;

    public GateTask(int id) {
        this.passengerId = id;
    }

    @Override
    public void run() {
        final String thread = Thread.currentThread().getName();
        System.out.println("Gate serving passenger " + passengerId + " on " + thread);

        try {
            //Passenger processing takes a while
            Thread.sleep(200);
            System.out.println("Task for passenger " + passengerId + " finished on " + thread);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Task for passenger " + passengerId + " interrupted on " + thread);
        }
    }
}
