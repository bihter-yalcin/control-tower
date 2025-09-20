package lab.exp01_threads;

public class ParallelJoinDemo {

    public static void main(String[] args) throws InterruptedException{
        Thread t1 = new Thread(new PassangerTask(1), "Passenger-1");
        Thread t2 = new Thread(new PassangerTask(2), "Passenger-2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("All passengers processed.");
    }
}
