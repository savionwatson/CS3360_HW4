/**
 * Stores the arrival and service information for a simulated process.
 */
public class SimProcess {
  private final int processId;
  private final double arrivalTime;
  private final double serviceTime;

  /**
   * Creates a process with its generated arrival and service times.
   *
   * @param processId unique process identifier
   * @param arrivalTime process arrival time
   * @param serviceTime requested CPU service time
   */
  public SimProcess(int processId, double arrivalTime, double serviceTime) {
    this.processId = processId;
    this.arrivalTime = arrivalTime;
    this.serviceTime = serviceTime;
  }

  /**
   * Returns this process's identifier.
   *
   * @return process identifier
   */
  public int getProcessId() {
    return processId;
  }

  /**
   * Returns this process's arrival time.
   *
   * @return arrival time
   */
  public double getArrivalTime() {
    return arrivalTime;
  }

  /**
   * Returns this process's requested service time.
   *
   * @return service time
   */
  public double getServiceTime() {
    return serviceTime;
  }
}
