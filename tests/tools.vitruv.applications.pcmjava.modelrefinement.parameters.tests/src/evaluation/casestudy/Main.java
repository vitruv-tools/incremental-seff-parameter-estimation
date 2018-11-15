package evaluation.casestudy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import evaluation.CsvWriter;
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

        /*runMode(Mode.Iteration0, Mode.Iteration0);
        runMode(Mode.Iteration1, Mode.Iteration1);
        runMode(Mode.Complete, Mode.Iteration0);
        runMode(Mode.Complete, Mode.Iteration1);
*/
        runMode(Mode.Nothing, Mode.Iteration0);
        runMode(Mode.Nothing, Mode.Iteration1);

        System.out.println("Finished performance monitoring.");
    }

    private static void runMode(Mode logMode, Mode runMode) throws Exception {
        Common c = new Common(logMode, runMode);
        ThreadMonitoringController.setSessionId(c.getSessionId());
        
        runModeThreads(c, 5, 5);
    }

    private static void runModeThreads(Common c, int thinkTimeMillis, int population) throws IOException, InterruptedException {
        
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    this.internRun();
                } catch (Exception e) {
                }
            }

            private void internRun() throws Exception {
                Random rand = ThreadLocalRandom.current();
                
                Thread.sleep(rand.nextInt(thinkTimeMillis));
                
                CsvWriter csv2 = null;
                if (c.logMode == Mode.Nothing) {
                    csv2 = new CsvWriter(
                            Common.DataRootPath + "response-time-logs/threaded/" + c.getSessionId() + "/threadid-"
                                    + Thread.currentThread().getId() + ".csv",
                            "argument", "iterations");
                }

                B b = new B(c);
                A a = new A(b, c);

                for (int x = 0; x < 500; x++) {

                    long startTime = System.nanoTime();
                    int i = rand.nextInt(10);
                    a.methodA(i);
                    long stopTime = System.nanoTime();
                    if (csv2 != null) {
                        long timeDiff = stopTime - startTime;
                        csv2.write(i, timeDiff);
                    }
                    
                    Thread.sleep(thinkTimeMillis);
                    
                    if (x % 100 == 0) {
                        System.out.println("Next step thread: " + Thread.currentThread().getId());
                    }
                }

                if (csv2 != null) {
                    csv2.close();
                }
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

    private static void runModeDefault(Common c) throws IOException {

        CsvWriter csv2 = null;
        if (c.logMode == Mode.Nothing) {
            csv2 = new CsvWriter(Common.DataRootPath + "response-time-logs/" + c.getSessionId() + "data.csv",
                    "argument", "iterations");
        }

        for (int x = 0; x < 50; x++) {
            B b = new B(c);
            A a = new A(b, c);

            for (int i = 0; i < 10; i++) {
                long startTime = System.nanoTime();
                a.methodA(i);
                long stopTime = System.nanoTime();
                if (csv2 != null) {
                    long timeDiff = stopTime - startTime;
                    csv2.write(i, timeDiff);
                }
            }

            System.out.println("Next application run finished.");
        }

        if (csv2 != null) {
            csv2.close();
        }

        System.out.println("Finished " + c.getSessionId());
    }
}
