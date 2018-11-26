package evaluation.casestudy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import evaluation.utils.CsvWriter;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.sampler.sigar.ISigarSamplerFactory;
import kieker.monitoring.sampler.sigar.SigarSamplerFactory;
import kieker.monitoring.sampler.sigar.samplers.CPUsDetailedPercSampler;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class Main {

    final static ISigarSamplerFactory sigarFactory = SigarSamplerFactory.INSTANCE;

    final static CPUsDetailedPercSampler cpuSampler = sigarFactory.createSensorCPUsDetailedPerc();

    public static void main(final String[] args) throws Exception {

        MonitoringController.getInstance().schedulePeriodicSampler(
                cpuSampler, 0, 1, TimeUnit.SECONDS);

        System.out.println("Set processor affinity to CPU 0 and press enter.");
        System.in.read();

        // evaluation 1 (in thesis)
        evaluationEstimationRun(500, 0, 1, "default");
        evaluationCompareRun(500, 5, 10, "default-changed");
        evaluationEstimationRun(500, 5, 5, "threaded");
        evaluationCompareRun(500, 5, 10, "threaded-changed");
     
        // evaluation 2
        evaluationEstimationRun(100, 500, 5, "threaded-2");
        evaluationCompareRun(100, 500, 10, "threaded-2-changed");

        System.out.println("Finished performance monitoring.");
    }
    
    private static void evaluationCompareRun(int iterations, int thinkTimeMillis, int population, String name) throws Exception {
        evaluationRun(Mode.Nothing, Mode.Iteration0, iterations, thinkTimeMillis, population, name);
        evaluationRun(Mode.Nothing, Mode.Iteration1, iterations, thinkTimeMillis, population, name);
    }
    
    private static void evaluationEstimationRun(int iterations, int thinkTimeMillis, int population, String name) throws Exception {
        evaluationRun(Mode.Iteration0, Mode.Iteration0, iterations, thinkTimeMillis, population, name);
        evaluationRun(Mode.Iteration1, Mode.Iteration1, iterations, thinkTimeMillis, population, name);
        evaluationRun(Mode.Complete, Mode.Iteration1, iterations, thinkTimeMillis, population, name);

        evaluationRun(Mode.Nothing, Mode.Iteration0, iterations, thinkTimeMillis, population, name);
        evaluationRun(Mode.Nothing, Mode.Iteration1, iterations, thinkTimeMillis, population, name);
    }

    private static void evaluationRun(Mode logMode, Mode runMode, int iterations, int thinkTimeMillis, int population, String name) throws Exception {
        Common c = new Common(logMode, runMode, name);
        ThreadMonitoringController.setSessionId(c.getSessionId());

        evaluationThreadedRun(c, iterations, thinkTimeMillis, population);
    }

    private static void evaluationThreadedRun(Common c, int iterations, int thinkTimeMillis, int population) throws Exception {

        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    this.internRun();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }

            private void internRun() throws Exception {
                Random rand = ThreadLocalRandom.current();

                if (thinkTimeMillis > 0) {
                    Thread.sleep(rand.nextInt(thinkTimeMillis));
                }
                
                CsvWriter csv2 = new CsvWriter(c.getResponseTimeResultPath() + "threadid-"
                                + Thread.currentThread().getId() + ".csv",
                        "argument", "iterations");

                B b = new B(c);
                A a = new A(b, c);

                for (int x = 0; x < iterations; x++) {

                    long startTime = System.nanoTime();
                    int i = rand.nextInt(10);
                    a.methodA(i);
                    long stopTime = System.nanoTime();
                    if (csv2 != null) {
                        long timeDiff = stopTime - startTime;
                        csv2.write(i, timeDiff);
                    }

                    if (thinkTimeMillis > 0) {
                        Thread.sleep(thinkTimeMillis);
                    }
                    
                    if (x % 100 == 0) {
                        System.out.println(
                                "Next step thread: " + Thread.currentThread().getId() + ": " + String.valueOf(x));
                    }
                }

                csv2.close();
            }
        };

        List<Thread> threads = new ArrayList<Thread>();

        for (int i = 0; i < population; i++) {
            Thread thread = new Thread(task);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("Finished " + c.getSessionId());
    }
}
