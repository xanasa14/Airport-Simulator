package airportsim;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceStation implements Runnable {
    private static int ID = 1;
    
    private int id;
    private ServiceType type;
    private ServiceType currentPassengerType = null;
    private long busyMillis = 0;
    
    private boolean busy = false;
    
    public ServiceStation(ServiceType type) {
        this.id = ServiceStation.ID++;
        this.type = type;
    }
    
    public ServiceType getType() {
        return this.type;
    }
    
    public int getId() {
        return id;
    }
    
    public boolean isBusy() {
        return this.busy;
    }
    
    public void serve(ServiceType passengerType) {
        if (this.busy) {
            Logger.getLogger(ServiceStation.class.getName()).log(Level.SEVERE, null, new Exception("Busy station"));
        }
        
        this.currentPassengerType = passengerType;
        this.busy = true;
        new Thread(this).start();
    }
    
    public double computeOccupancyRate(long totalMillis) {
        return (((double) busyMillis) / ((double) totalMillis)) * 100.0;
    }
    
    public void run() {
        try {
            double averageServiceTime = 0.0;
            if (this.currentPassengerType == ServiceType.FIRSTCLASS) {
                averageServiceTime = Airport.FIRSTCLASS_STATION_AVERAGE_SERVICE_TIME;
            } else if (this.currentPassengerType == ServiceType.COACH) {
                averageServiceTime = Airport.COACH_STATION_AVERAGE_SERVICE_TIME;
            }
            
            double realServiceTime = Utils.randomMinutes(averageServiceTime);
            long timeToWait = Utils.minsToMillis(Utils.realMinutesToSimulationMinutes(realServiceTime));
            
            System.out.println(this.type.name() + " station (" + this.id + ") will be busy serving a " + this.currentPassengerType.name() + " passenger for " + realServiceTime + " minutes");
            busyMillis += timeToWait;
            Thread.sleep(timeToWait);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServiceStation.class.getName()).log(Level.SEVERE, null, ex);
        }

        busy = false;
        System.out.println(this.type.name() + " station (" + this.id + ") is free");
        Airport.notifyFreeStation(this);
    }
}
