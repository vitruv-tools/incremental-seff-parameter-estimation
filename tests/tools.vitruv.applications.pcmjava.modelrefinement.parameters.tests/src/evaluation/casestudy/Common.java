package evaluation.casestudy;

import java.io.File;

import evaluation.dependencies.Action;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class Common {
    
    public static final int ComputationConst1 = 3;
    
    public static final int ComputationConst2 = 10;
    
    public static String DataRootPath = "./test-data/casestudy/";
    
    public static String ResultsPath = DataRootPath + "results-data-2/";
    
    public final Mode logMode;

    public final Mode runMode;
    
    public final String name;
    
    public Common(Mode mode, Mode runMode, String name) {
        this.logMode = mode;
        this.runMode = runMode;
        this.name = name;
    }
    
    public String getResponseTimeResultPath() {
        String path = ResultsPath + this.name + "/" + getFileSessionId() + "/response-time/";
        createDirectory(path);
        return path;
    }
    
    public String getUtilizationResultPath() {
        String path = ResultsPath + this.name + "/" + getFileSessionId() + "/utilization/";
        createDirectory(path);
        return path;
    }
    
    public String getSimulatedResponseTimeResultPath() {
        String path = ResultsPath + this.name + "/" + getFileSessionId() + "/simulated-response-time/";
        createDirectory(path);
        return path;
    }
    
    public String getFileSessionId() {
        return "session-log-" + logMode + "-run-" + runMode;
    }
    
    public String getSessionId() {
        return "session-" + this.name + "-log-" + logMode + "-run-" + runMode;
    }
    
    public void createDirectory(String dirPath) {
        File file = new File(dirPath);
        file.getParentFile().mkdirs();
    }
    
    public static void computation(final int param) {
        double[] array = new double[1000 * param];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.sqrt(i);
            for (int a = 0; a < array.length; a++) {
                array[i] += array[a];
            }
        }
    }
    
    public static final String CpuResourceId = "_oro4gG3fEdy4YaaT-RYrLQ";
    
    public boolean mustLog(Mode mode) {
        return this.logMode == mode || this.logMode == Mode.Complete;
    }
    
    public void setCallerId(String callerId) {
        if (this.logMode != Mode.Nothing) {
            ThreadMonitoringController.getInstance().setCurrentCallerId(callerId);
        }
    }
    
    public boolean mustRun(Mode runMode) {
        return this.runMode == runMode || this.runMode == Mode.Complete;
    }

    public void logBranchOnIteration(Mode mode, String branchId, String branchTransitionId) {
        if (this.logMode == mode || this.logMode == Mode.Complete) {
            // Monitoring actions start
            ThreadMonitoringController.getInstance().logBranchExecution(branchId, branchTransitionId);
            // Monitoring actions end
        }
    }
    
    public void logOnIteration(Mode mode, String loopId, long iterations) {
        if (this.logMode == mode || this.logMode == Mode.Complete) {
            // Monitoring actions start
            ThreadMonitoringController.getInstance().logLoopIterationCount(loopId, iterations);
            // Monitoring actions end
        }
    }
    
    public void whenOn(Action action) {
        if (this.logMode != Mode.Nothing) {
            action.execute();
        }
    }

    public void logOnIteration(Mode mode, String internalActionId, Action action) {
        long ___startTime_2 = 0;
        if (this.logMode == mode || this.logMode == Mode.Complete) {
            // Monitoring actions start
            ___startTime_2 = ThreadMonitoringController.getInstance().getTime();
            // Monitoring actions end
        }

        action.execute();

        if (this.logMode == mode || this.logMode == Mode.Complete) {
            // Monitoring actions start
            ThreadMonitoringController.getInstance().logResponseTime(internalActionId, CpuResourceId, ___startTime_2);
            // Monitoring actions end
        }
    }
}
