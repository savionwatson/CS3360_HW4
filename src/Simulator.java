import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Simulates an M/M/1 queue using FCFS or non-preemptive SJF scheduling.
 */
public class Simulator {
  private static final int ARRIVAL = 0;
  private static final int DEPARTURE = 1;
  private static final int MAX_PROCESSES = 10000;

  private double arrivalRate;
  private double meanServiceTime;
  private int scheduler;
  private double clock;
  private int completedProcesses;
  private final PriorityQueue<Event> eventQueue =
      new PriorityQueue<>(Comparator.comparingDouble(Event::getEventTime));
  private PriorityQueue<SimProcess> readyQueue;
  private final Random random = new Random();
  private int processesCreated;
  private boolean cpuBusy;

  /**
   * Creates a simulator with default parameter values.
   */
  Simulator() {
    arrivalRate = 0;
    meanServiceTime = 0;
    scheduler = 0;
    clock = 0;
    completedProcesses = 0;
    initializeReadyQueue();
  }

  /**
   * Creates a simulator with the specified arrival rate, service time, and
   * scheduling algorithm.
   *
   * @param arrivalRate average process arrivals per second
   * @param meanServiceTime average service time in seconds
   * @param scheduler 0 for FCFS or 1 for SJF
   */
  Simulator(double arrivalRate, double meanServiceTime, int scheduler) {
    this.arrivalRate = arrivalRate;
    this.meanServiceTime = meanServiceTime;
    this.scheduler = scheduler;
    clock = 0;
    completedProcesses = 0;
    initializeReadyQueue();
  }

  /**
   * Creates the ready queue ordered according to the selected scheduler.
   */
  private void initializeReadyQueue() {
    Comparator<SimProcess> processOrder;
    if (scheduler == 0) {
      processOrder = Comparator.comparingDouble(SimProcess::getArrivalTime)
          .thenComparingInt(SimProcess::getProcessId);
    } else {
      processOrder = Comparator.comparingDouble(SimProcess::getServiceTime)
          .thenComparingDouble(SimProcess::getArrivalTime)
          .thenComparingInt(SimProcess::getProcessId);
    }
    readyQueue = new PriorityQueue<>(processOrder);
  }

  /**
   * Returns the configured average arrival rate.
   *
   * @return average arrivals per second
   */
  public double getArrivalRate() {
    return arrivalRate;
  }

  /**
   * Sets the average arrival rate.
   *
   * @param arrivalRate average arrivals per second
   */
  void setArrivalRate(double arrivalRate) {
    this.arrivalRate = arrivalRate;
  }

  /**
   * Returns the configured average service time.
   *
   * @return average service time in seconds
   */
  public double getMeanServiceTime() {
    return meanServiceTime;
  }

  /**
   * Sets the average service time.
   *
   * @param meanServiceTime average service time in seconds
   */
  void setMeanServiceTime(double meanServiceTime) {
    this.meanServiceTime = meanServiceTime;
  }

  /**
   * Returns the selected scheduling algorithm.
   *
   * @return 0 for FCFS or 1 for SJF
   */
  public int getScheduler() {
    return scheduler;
  }

  /**
   * Sets the scheduling algorithm.
   *
   * @param scheduler 0 for FCFS or 1 for SJF
   */
  void setScheduler(int scheduler) {
    this.scheduler = scheduler;
  }

  /**
   * Handles a process arrival by starting service or adding it to the ready
   * queue, then schedules the next arrival.
   *
   * @param event arrival event to process
   */
  private void handleArrival(Event event) {
    SimProcess arrivingProcess = event.getProcess();

    if (cpuBusy) {
      readyQueue.add(arrivingProcess);
    } else {
      startService(arrivingProcess);
    }

    scheduleNextArrival(event.getEventTime());
  }

  /**
   * Handles a process departure by idling the CPU or starting the next process.
   */
  private void handleDeparture() {
    if (readyQueue.isEmpty()) {
      cpuBusy = false;
    } else {
      startService(readyQueue.poll());
    }
  }

  /**
   * Starts a process and adds its future departure to the event queue.
   *
   * @param process process to start
   */
  private void startService(SimProcess process) {
    cpuBusy = true;
    double departureTime = clock + process.getServiceTime();
    eventQueue.add(new Event(departureTime, DEPARTURE, process));
  }

  /**
   * Generates the next process and schedules its arrival event.
   *
   * @param previousArrivalTime time of the preceding arrival
   */
  private void scheduleNextArrival(double previousArrivalTime) {
    if (processesCreated >= MAX_PROCESSES) {
      return;
    }

    double arrivalTime =
        previousArrivalTime + exponentialSample(1.0 / arrivalRate);
    double serviceTime = exponentialSample(meanServiceTime);
    SimProcess process =
        new SimProcess(processesCreated, arrivalTime, serviceTime);
    processesCreated++;
    eventQueue.add(new Event(arrivalTime, ARRIVAL, process));
  }

  /**
   * Samples a random value from an exponential distribution.
   *
   * @param mean mean of the exponential distribution
   * @return a nonnegative random sample
   */
  private double exponentialSample(double mean) {
    return -mean * Math.log(1.0 - random.nextDouble());
  }

  /**
   * Processes events until the configured number of processes have completed,
   * then prints the simulation metrics.
   */
  public void runSimulation() {
    System.out.println(
        "Running simulation with arrival rate: " + arrivalRate
            + ", mean service time: " + meanServiceTime
            + ", scheduler: " + (scheduler == 0 ? "FCFS" : "SJF"));

    double totalTurnaroundTime = 0.0;
    double totalBusyTime = 0.0;
    double readyQueueArea = 0.0;

    scheduleNextArrival(0);
    while (completedProcesses < MAX_PROCESSES) {
      Event event = eventQueue.poll();

      if (event == null) {
        throw new IllegalStateException(
            "No events remain before the simulation completed.");
      }

      double elapsedTime = event.getEventTime() - clock;
      if (cpuBusy) {
        totalBusyTime += elapsedTime;
      }
      readyQueueArea += readyQueue.size() * elapsedTime;
      clock = event.getEventTime();

      if (event.getEventType() == ARRIVAL) {
        handleArrival(event);
      } else {
        handleDeparture();
        completedProcesses++;
        totalTurnaroundTime +=
            event.getEventTime() - event.getProcess().getArrivalTime();
      }
    }

    System.out.println("Completed processes: " + completedProcesses);
    System.out.printf(
        "Average turnaround time: %.2f seconds%n",
        totalTurnaroundTime / completedProcesses);
    System.out.printf(
        "Throughput: %.2f processes per second%n",
        completedProcesses / clock);
    System.out.printf(
        "CPU Utilization: %.2f%%%n",
        totalBusyTime / clock * 100.0);
    System.out.printf(
        "Average number of processes in Ready Queue: %.2f%n",
        readyQueueArea / clock);
  }

  /**
   * Validates command-line arguments and runs the requested simulation.
   *
   * @param args arrival rate, mean service time, and scheduler selection
   */
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(
          "Usage: java Simulator <arrival-rate (10-30)> "
              + "<mean-service-time> <scheduler (0=FCFS, 1=SJF)>");
      return;
    }

    try {
      double arrivalRate = Double.parseDouble(args[0]);
      double meanServiceTime = Double.parseDouble(args[1]);
      int scheduler = Integer.parseInt(args[2]);

      if (!Double.isFinite(arrivalRate)
          || arrivalRate < 10
          || arrivalRate > 30) {
        System.err.println("Arrival rate must be between 10 and 30.");
        return;
      }
      if (!Double.isFinite(meanServiceTime) || meanServiceTime <= 0) {
        System.err.println("Mean service time must be a positive number.");
        return;
      }
      if (scheduler != 0 && scheduler != 1) {
        System.err.println("Scheduler must be 0 (FCFS) or 1 (SJF).");
        return;
      }

      Simulator sim = new Simulator(arrivalRate, meanServiceTime, scheduler);
      sim.runSimulation();
    } catch (NumberFormatException e) {
      System.err.println(
          "All arguments must be numeric. Scheduler must be a whole number.");
      System.err.println(
          "Usage: java Simulator <arrival-rate (10-30)> "
              + "<mean-service-time> <scheduler (0=FCFS, 1=SJF)>");
    }
  }
}
