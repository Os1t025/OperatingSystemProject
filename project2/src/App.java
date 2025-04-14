import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

// The example java thread
class ProcessThread extends Thread {
    int pid, burstTime;

    public ProcessThread(int pid, int burstTime) {
        this.pid = pid;
        this.burstTime = burstTime;
    }

    public void run() {
        System.out.println("Process " + pid + " started.");
        try {
            Thread.sleep(burstTime * 1000);
        } catch (InterruptedException e) {
            System.out.println("Process " + pid + " interrupted.");
        }
        System.out.println("Process " + pid + " finished.");
    }
}

// 2.1 Dining philosopher problem
class Philosopher extends Thread {
    private final int id;
    private final Lock leftFork;
    private final Lock rightFork;

    public Philosopher(int id, Lock leftFork, Lock rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
    }

    private void log(String message) {
        System.out.println("[Philosopher " + id + "] " + message);
    }

    public void run() {
        try {
            while (true) {
                log("Thinking...");
                Thread.sleep((int)(Math.random() * 1000));

                log("Waiting for forks...");
                // Trying to avoid deadlock by picking up forks in order
                Lock first = (id % 2 == 0) ? leftFork : rightFork;
                Lock second = (id % 2 == 0) ? rightFork : leftFork;

                first.lock();
                log("Picked up first fork.");
                second.lock();
                log("Picked up second fork. Eating...");

                Thread.sleep((int)(Math.random() * 1000));

                log("Releasing forks.");
                second.unlock();
                first.unlock();
            }
        } catch (InterruptedException e) {
            log("Stopped.");
        }
    }
}

public class App {
    public static void main(String[] args) throws Exception {
        // Run processes from txt file
        System.out.println("=== Simulating Processes from processes.txt ===");
        List<ProcessThread> processes = readProcesses("processes.txt");
        for (ProcessThread pt : processes) {
            pt.start();
        }
        for (ProcessThread pt : processes) {
            pt.join();
        }

        // Start philosopher threads
        System.out.println("\n=== Dining Philosophers Simulation ===");
        int NUM_PHILOSOPHERS = 5;
        Lock[] forks = new Lock[NUM_PHILOSOPHERS];
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            forks[i] = new ReentrantLock();
        }

        Philosopher[] philosophers = new Philosopher[NUM_PHILOSOPHERS];
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            Lock leftFork = forks[i];
            Lock rightFork = forks[(i + 1) % NUM_PHILOSOPHERS];
            philosophers[i] = new Philosopher(i, leftFork, rightFork);
            philosophers[i].start();
        }

        Thread.sleep(1000);
        for (Philosopher p : philosophers) {
            p.interrupt();
        }
    }

    // Reads process data from file
    private static List<ProcessThread> readProcesses(String filename) {
        List<ProcessThread> processes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                int pid = Integer.parseInt(parts[0]);
                int burstTime = Integer.parseInt(parts[2]);
                processes.add(new ProcessThread(pid, burstTime));
            }
        } catch (IOException e) {
            System.out.println("Error reading process file: " + e.getMessage());
        }
        return processes;
    }
}


