package airportsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Airport {
    private static int FIRSTCLASS_SERVICE_STATIONS_COUNT = 2;
    private static int COACH_SERVICE_STATIONS_COUNT = 3;
    
    private static double CHECK_IN_DURATION_TIME = 40.0;
    
    private static double FIRSTCLASS_PASSENGER_AVERAGE_ARRIVAL_TIME = 5.0;
    private static double COACH_PASSENGER_AVERAGE_ARRIVAL_TIME = 2.0;
    
    static double FIRSTCLASS_STATION_AVERAGE_SERVICE_TIME = 6.0;
    static double COACH_STATION_AVERAGE_SERVICE_TIME = 2.0;
    
    private static boolean CHOOSE_RANDOM_STATION = true;
    
    private static Queue firstClassQueue;
    private static Queue coachQueue;
    
    private static long startTimestamp = 0;
    private static boolean hasClosedQueues = false;
    
    private static final ArrayList<ServiceStation> firstClassServiceStations = new ArrayList<ServiceStation> ();
    private static final ArrayList<ServiceStation> coachServiceStations = new ArrayList<ServiceStation> ();
    
    public static void main(String[] args) {
        System.out.println("Please enter the following parameters");
        Scanner keyboard = new Scanner(System.in);
        System.out.print("Check in duration (in minutes): ");
        CHECK_IN_DURATION_TIME = keyboard.nextDouble();
        
        System.out.print("First class passenger average arrival time (in minutes): ");
        FIRSTCLASS_PASSENGER_AVERAGE_ARRIVAL_TIME = keyboard.nextDouble();
        
        System.out.print("First class average service rate (in minutes): ");
        FIRSTCLASS_STATION_AVERAGE_SERVICE_TIME = keyboard.nextDouble();
        
        System.out.print("Coach passenger average arrival time (in minutes): ");
        COACH_PASSENGER_AVERAGE_ARRIVAL_TIME = keyboard.nextDouble();
        
        System.out.print("Coach average service rate (in minutes): ");
        COACH_STATION_AVERAGE_SERVICE_TIME = keyboard.nextDouble();
        
        System.out.print("Should the passengers choose a random station? (yes/no) (default yes): ");
        keyboard.nextLine();
        String randomStation = keyboard.nextLine().trim();
        
        if (!randomStation.isEmpty()) {
            CHOOSE_RANDOM_STATION = randomStation.equals("yes");
        }
        
        System.out.print("Simulation vs real time factor (1 simulation minute = 1 real minute * factor) (default 0.001): ");
        String factor = keyboard.nextLine().trim();
        if (!factor.isEmpty()) {
            Utils.REALTIME_FACTOR = Double.parseDouble(factor);
        }
        
        System.out.println();
        
        for (int i = 0; i < FIRSTCLASS_SERVICE_STATIONS_COUNT; i++) {
            ServiceStation station = new ServiceStation(ServiceType.FIRSTCLASS);
            firstClassServiceStations.add(station);
        }
        
        for (int i = 0; i < COACH_SERVICE_STATIONS_COUNT; i++) {
            ServiceStation station = new ServiceStation(ServiceType.COACH);
            coachServiceStations.add(station);
        }
        
        startSimulation();
    }
    
    private static void startSimulation() {
        startTimestamp = System.currentTimeMillis();
        
        firstClassQueue = new Queue(ServiceType.FIRSTCLASS, FIRSTCLASS_PASSENGER_AVERAGE_ARRIVAL_TIME);
        coachQueue = new Queue(ServiceType.COACH, COACH_PASSENGER_AVERAGE_ARRIVAL_TIME);
    
        new Thread(firstClassQueue).start();
        new Thread(coachQueue).start();
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long timeToClose = Utils.minsToMillis(Utils.realMinutesToSimulationMinutes(CHECK_IN_DURATION_TIME));
                    Thread.sleep(timeToClose);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Airport.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                System.out.println("*********************** Closing queues ***********************");
                Airport.closeQueues();
            }
        }).start();
    }
    
    private static void printReport() {
        System.out.println();
        long elapsedMillis = System.currentTimeMillis() - startTimestamp;
        
        double elapsedRealMinutes = Utils.simulationMinutesToRealTimeMinutes(Utils.millisToMinutes(elapsedMillis));
        System.out.println("------ Simulation ended after " + elapsedRealMinutes + " minutes ------");
        System.out.println(ServiceType.FIRSTCLASS.name() + " queue:");
        System.out.println("\tMax passengers: " + firstClassQueue.getMaxLength());
        System.out.println("\tAverage waiting time: " + firstClassQueue.getAverageWaitingTime() + " minutes");
        System.out.println("\tMax waiting time: " + firstClassQueue.getMaxWaitingTime() + " minutes");
        
        System.out.println();
        System.out.println(ServiceType.COACH.name() + " queue:");
        System.out.println("\tMax passengers: " + coachQueue.getMaxLength());
        System.out.println("\tAverage waiting time: " + coachQueue.getAverageWaitingTime() + " minutes");
        System.out.println("\tMax waiting time: " + coachQueue.getMaxWaitingTime() + " minutes");
        
        System.out.println();
        System.out.println(ServiceType.FIRSTCLASS.name() + " stations:");
        for (ServiceStation firstClassServiceStation : firstClassServiceStations) {
            System.out.println("\t(" + firstClassServiceStation.getId() + ") rate of occupancy: " + firstClassServiceStation.computeOccupancyRate(elapsedMillis) + "%");
        }
        
        System.out.println();
        System.out.println(ServiceType.COACH.name() + " stations:");
        for (ServiceStation coachServiceStation : coachServiceStations) {
            System.out.println("\t(" + coachServiceStation.getId() + ") rate of occupancy: " + coachServiceStation.computeOccupancyRate(elapsedMillis) + "%");
        }
        
        System.out.println();
    }
    
    public static void closeQueues() {
        Airport.hasClosedQueues = true;
        Airport.firstClassQueue.close();
        Airport.coachQueue.close();
        
        Airport.verifyIfCheckInEnded();
    }
    
    private static void verifyIfCheckInEnded() {
        if (!Airport.hasClosedQueues) {
            return;
        }
        
        boolean hasPassengers = firstClassQueue.hasPassengers() || coachQueue.hasPassengers();
        if (hasPassengers) {
            return;
        }

        boolean hasBusyStations = false;
        for (ServiceStation coachServiceStation : coachServiceStations) {
            if (coachServiceStation.isBusy()) {
                hasBusyStations = true;
                break;
            }
        }

        if (hasBusyStations) {
            return;
        }

        for (ServiceStation firstClassServiceStation : firstClassServiceStations) {
            if (firstClassServiceStation.isBusy()) {
                hasBusyStations = true;
                break;
            }
        }

        if (hasBusyStations) {
            return;
        }

        printReport();
    }
    
    public static void notifyFreeStation(ServiceStation station) {
        if (station.getType() == ServiceType.FIRSTCLASS) {
            if (firstClassQueue.hasPassengers() && !station.isBusy()) {
                firstClassQueue.dequeue();
                station.serve(ServiceType.FIRSTCLASS);
            } else if (coachQueue.hasPassengers() && !station.isBusy()) {
                coachQueue.dequeue();
                station.serve(ServiceType.COACH);
            }
        } else if (station.getType() == ServiceType.COACH) {
            if (coachQueue.hasPassengers() && !station.isBusy()) {
                coachQueue.dequeue();
                station.serve(ServiceType.COACH);
            }
        }
        
        verifyIfCheckInEnded();
    }
    
    public static void notifyNewArrival(Queue queue) {
        boolean hasAssignedStation = false;
        
        if (CHOOSE_RANDOM_STATION) {
            Collections.shuffle(firstClassServiceStations);
            Collections.shuffle(coachServiceStations);
        }
        
        if (queue.getType() == ServiceType.COACH) {
            for (ServiceStation coachServiceStation : coachServiceStations) {
                if (!coachServiceStation.isBusy()) {
                    if (queue.hasPassengers()) {
                        queue.dequeue();
                        coachServiceStation.serve(ServiceType.COACH);
                    }
                    
                    hasAssignedStation = true;
                    break;
                }
            }
            
            if (!hasAssignedStation) {
                for (ServiceStation firstClassServiceStation : firstClassServiceStations) {
                    if (!firstClassServiceStation.isBusy()) {
                        if (queue.hasPassengers()) {
                            queue.dequeue();
                            firstClassServiceStation.serve(ServiceType.COACH);
                        }

                        break;
                    }
                }
            }
        } else if (queue.getType() == ServiceType.FIRSTCLASS) {
            for (ServiceStation firstClassServiceStation : firstClassServiceStations) {
                if (!firstClassServiceStation.isBusy()) {
                    if (queue.hasPassengers()) {
                        queue.dequeue();
                        firstClassServiceStation.serve(ServiceType.FIRSTCLASS);
                    }
                    
                    break;
                }
            }
        }
    }
}
