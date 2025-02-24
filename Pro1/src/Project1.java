import java.io.*;
import java.util.*;

class Process {
    int pid;
    int arrivalTime;
    int burstTime;
    int startTime;
    int endTime;
    int waitingTime;
    int turnaroundTime;
    int remainingTime;

    public Process(int pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public void calculateTimes() {
        turnaroundTime = endTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
    }
}

public class Project1 {
    private static List<Process> processes = new ArrayList<>();
    private static final int TIME_QUANTUM = 4;
    public static void readProcessesFromFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");
                int pid = Integer.parseInt(parts[0]);
                int arrivalTime = Integer.parseInt(parts[1]);
                int burstTime = Integer.parseInt(parts[2]);

                processes.add(new Process(pid, arrivalTime, burstTime));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // First-Come, First-Served
    public static void FCFS() {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.startTime = currentTime;
            p.endTime = currentTime + p.burstTime;
            currentTime = p.endTime;
            p.calculateTimes();
        }
        displayGanttChart("FCFS");
    }

    // Shortest Job First
    public static void SJF() {
        List<Process> processCopy = new ArrayList<>(processes);
        processCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;
        List<Process> readyQueue = new ArrayList<>();
    
        while (!processCopy.isEmpty() || !readyQueue.isEmpty()) {
            while (!processCopy.isEmpty() && processCopy.get(0).arrivalTime <= currentTime) {
                readyQueue.add(processCopy.remove(0));
            }
    
            if (!readyQueue.isEmpty()) {
                readyQueue.sort(Comparator.comparingInt(p -> p.burstTime));
                Process p = readyQueue.remove(0);
    
                if (currentTime < p.arrivalTime) {
                    currentTime = p.arrivalTime;
                }
                p.startTime = currentTime;
                p.endTime = currentTime + p.burstTime;
                currentTime = p.endTime;
                p.calculateTimes();
            } else {
                currentTime++;
            }
        }
        displayGanttChart("SJF");
    }
    //Round Robin
    public static void RR() {
        Queue<Process> readyQueue = new LinkedList<>();
        int currentTime = 0;

        List<Process> processCopy = new ArrayList<>(processes);
        processCopy.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (!processCopy.isEmpty() || !readyQueue.isEmpty()) {
            while (!processCopy.isEmpty() && processCopy.get(0).arrivalTime <= currentTime) {
                readyQueue.add(processCopy.remove(0));
            }

            if (!readyQueue.isEmpty()) {
                Process p = readyQueue.poll();
                int timeSlice = Math.min(TIME_QUANTUM, p.remainingTime);
                p.remainingTime -= timeSlice;
                currentTime += timeSlice;

                if (p.remainingTime == 0) {
                    p.endTime = currentTime;
                    p.calculateTimes();
                } else {
                    readyQueue.add(p);
                }

                if (p.startTime == 0) {
                    p.startTime = currentTime - timeSlice;
                }
            } else {
                currentTime++;
            }
        }
        displayGanttChart("RR");
    } 
    
    // Display the Gantt Chart
    public static void displayGanttChart(String algorithm) {
        System.out.println("\n" + algorithm + " Gantt Chart:");
        int totalWT = 0, totalTAT = 0;

        for (Process p : processes) {
            System.out.print("| P" + p.pid + " ");
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.println("|");

        System.out.print("0 ");
        for (Process p : processes) {
            System.out.print("  " + p.endTime + " ");
        }
        System.out.println("\n");

        System.out.println("\nProcess Information:");
        for (Process p : processes) {
            System.out.println("P" + p.pid + " | Waiting Time: " + p.waitingTime + " | Turnaround Time: " + p.turnaroundTime);
        }

        System.out.println("\nAverage Waiting Time: " + (totalWT / (double) processes.size()));
        System.out.println("Average Turnaround Time: " + (totalTAT / (double) processes.size()));
    }

    public static void main(String[] args) {
        String filename = "processes.txt";
        readProcessesFromFile(filename);

        FCFS();
        SJF();
        RR();
    }
}


