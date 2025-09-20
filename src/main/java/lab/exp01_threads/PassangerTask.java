package lab.exp01_threads;

public class PassangerTask implements Runnable {
    private final int id;

    PassangerTask(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        String thread = Thread.currentThread().getName();
        System.out.println("Passanger "+ id + " arrived, handled by " + thread);

        try {
            Thread.sleep(200);
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        System.out.println("Passanger "+ id + " finished by " + thread);

    }
}