package airportsim;

import java.util.Random;

public class Utils {
    static double REALTIME_FACTOR = 0.001; // Relation between real time minutes vs program minutes (program minute = real time minute / REALTIME_FACTOR)
    private static final double MIN_TO_MILLIS = 60000.0;
    private static final Random RANDOM = new Random();
    
    public static final double randomMinutes(double realMinutesMean) {
        double randomTime = RANDOM.nextGaussian() * 0.5 + realMinutesMean;
        return Math.max(randomTime, 0.0);
    }
    
    public static long minsToMillis(double minutes) {
        return (long) (minutes * MIN_TO_MILLIS);
    }
    
    public static double millisToMinutes(long millis) {
        return ((double) millis) / MIN_TO_MILLIS;
    }
    
    public static double realMinutesToSimulationMinutes(double realMinutes) {
        return realMinutes * REALTIME_FACTOR;
    }
    
    public static double simulationMinutesToRealTimeMinutes(double simulationMinutes) {
        return simulationMinutes / REALTIME_FACTOR;
    }
}
