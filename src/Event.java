/**
 * Represents an arrival or departure at a specific simulation time.
 */
public class Event {
  private final double eventTime;
  private final int eventType;
  private final SimProcess process;

  /**
   * Creates an event for the specified process.
   *
   * @param eventTime time at which the event occurs
   * @param eventType event type, where 0 is arrival and 1 is departure
   * @param process process associated with the event
   */
  public Event(double eventTime, int eventType, SimProcess process) {
    this.eventTime = eventTime;
    this.eventType = eventType;
    this.process = process;
  }

  /**
   * Returns the time at which this event occurs.
   *
   * @return event time
   */
  public double getEventTime() {
    return eventTime;
  }

  /**
   * Returns this event's type.
   *
   * @return 0 for arrival or 1 for departure
   */
  public int getEventType() {
    return eventType;
  }

  /**
   * Returns the process associated with this event.
   *
   * @return associated process
   */
  public SimProcess getProcess() {
    return process;
  }
}
