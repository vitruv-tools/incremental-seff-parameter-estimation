package evaluation.logging;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class Main {
    
    public static void main(final String[] args) throws Exception {

        
        System.out.println("Set processor affinity to CPU 0 and press enter.");
        System.in.read();

        int iterations = 1000;
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < iterations; i++) {
            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.addInt("a", 10);
            ThreadMonitoringController.getInstance().enterService("_XYJcUMjPEeiWRYm1yDC5rQ", serviceParameters);
            
            ThreadMonitoringController.getInstance().exitService();
        }
        
        long stopTime = System.nanoTime();
        double average = (stopTime - startTime) / 1e6 / iterations;
        System.out.printf("Average monitoring response time: %.6f ms\n", average);
        
        System.out.println("Finished performance monitoring.");
    }
    
}
