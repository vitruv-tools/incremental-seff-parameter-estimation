package evaluation.logging;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class Main {

    public static void main(final String[] args) throws Exception {

        System.out.println("Set processor affinity to CPU 0 and press enter.");
        System.in.read();
        
        measureLoggingTime(2000);
        measureGetTime(20000);

        System.out.println("Finished performance monitoring.");
    }
    
    private static void measureGetTime(int iterations) {
        long timeSum = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            ThreadMonitoringController.getInstance().getTime();
            long stopTime = System.nanoTime();
            timeSum += stopTime - startTime; 
        }

        double average = ((double)timeSum) / iterations;
        average /= 1e6;
        System.out.printf("Average getting time: %.6f ms\n", average);
    }

    private static void measureLoggingTime(int iterations) {
        long timeSum = 0;

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            
            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.addInt("a", 10);
            ThreadMonitoringController.getInstance().enterService("_XYJcUMjPEeiWRYm1yDC5rQ", serviceParameters);

            ThreadMonitoringController.getInstance().exitService();
            
            long stopTime = System.nanoTime();
            timeSum += stopTime - startTime; 
        }

        double average = ((double)timeSum) / iterations;
        average /= 1e6;
        System.out.printf("Average writing log time: %.6f ms\n", average);
    }

}
