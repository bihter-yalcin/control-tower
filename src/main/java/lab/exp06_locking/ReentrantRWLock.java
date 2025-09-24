package lab.exp06_locking;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantRWLock {

    private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
    private String data = "two-lights-on";

    public void read(String name) {
        rw.readLock().lock();
        try {
            System.out.println(name + " came to the runway to see lights (READ) -> data " + data);
            Thread.sleep(1000);
            System.out.println(name + " finished reading ");
        } catch (InterruptedException e) {
        } finally {
            rw.readLock().unlock();
        }
    }

    public void write(String name, String newValue) {
        rw.writeLock().lock();
        try {
            System.out.println(name + " came to the runway to take off (WRITE) -> data " + data);
            Thread.sleep(1500);
            data = newValue;
            System.out.println(name + " finished writing " + data);

        } catch (InterruptedException e) {
        } finally {
            rw.writeLock().unlock();
        }
    }

    public static void main(String[] args) {
        ReentrantRWLock runway = new ReentrantRWLock();

        //Two readers (planes) approaches to the runway to read lights.

        new Thread(() -> runway.read("R1")).start();
        new Thread(() -> runway.read("R2")).start();

        // The plane approaches to take off.
        new Thread(() -> runway.write("W1", "three-lights-on")).start();
    }
}