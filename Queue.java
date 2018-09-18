package airportsim;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Queue implements Runnable {
    private ServiceType type;
    private double averageArrivalTime;
    
    private LinkedList<Long> arrivalTimeStamps = new LinkedList<Long>();
    private LinkedList<Long> waitingTimes = new LinkedList<Long>();
    
    private int length = 0;
    private int maxLength = 0;
    private boolean closed = false;
    
    public Queue(ServiceType type, double averageArrivalTime) {
        this.type = type;
        this.averageArrivalTime = averageArrivalTime;
    }
    
    public void close() {
        this.closed = true;
    }
    
    public boolean dequeue() {
        if (this.hasPassengers()) {
            long currentTimeStamp = System.currentTimeMillis();
            long arrivalTimeStamp = arrivalTimeStamps.removeFirst();
            long waitingTime = currentTimeStamp - arrivalTimeStamp;
            waitingTimes.add(waitingTime);
            
            this.length--;
            return true;
        }
        
        return false;
    }
    
    public boolean hasPassengers() {
        return this.length > 0;
    }
    
    public void enqueue() {
        this.length++;
        arrivalTimeStamps.add(System.currentTimeMillis());
        if (this.length > this.maxLength) {
            this.maxLength = this.length;
        }
        
        Airport.notifyNewArrival(this);
    }
    
    public int getMaxLength() {
        return this.maxLength;
    }
    
    public ServiceType getType() {
        return this.type;
    }
    
    public void clear() {
        this.length = 0;
        this.maxLength = 0;
        
        this.arrivalTimeStamps.clear();
        this.waitingTimes.clear();
    }
    
    public double getAverageWaitingTime() {
        long millisSum = 0;
        int count = 0;
        for (Long waitingTime : waitingTimes) {
            millisSum += waitingTime;
            count++;
        }
        
        double millisAverage = ((double) millisSum) / ((double) count);
        return Utils.simulationMinutesToRealTimeMinutes(Utils.millisToMinutes((long) millisAverage));
    }
    
    public double getMaxWaitingTime() {
        long max = 0;
        for (Long waitingTime : waitingTimes) {
            max = Math.max(waitingTime, max);
        }
        
        return Utils.simulationMinutesToRealTimeMinutes(Utils.millisToMinutes((long) max));
    }

    @Override
    public void run() {
        while (!this.closed) {
            try {
                long timeToWait = Utils.minsToMillis(Utils.realMinutesToSimulationMinutes(Utils.randomMinutes(this.averageArrivalTime)));
                Thread.sleep(timeToWait);
                
                if (!this.closed) {
                    System.out.println(this.type.name() + " passenger arrived");
                    this.enqueue();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Queue.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
