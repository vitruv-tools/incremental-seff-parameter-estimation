package evaluation.casestudy;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;

import evaluation.utils.CsvWriter;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.SeffParameterEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.data.SimpleTestData;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.ExportUtils;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class SeffParameterEstimationTest {

    public static KiekerMonitoringReader getReader(Common c) {
        return new KiekerMonitoringReader(Common.ResultsPath + "kieker/", c.getSessionId());
    }

    public static Repository loadPcmModel(Common c) {
        return PcmUtils.loadModel(Common.DataRootPath + c.runMode + "-model/default.repository");
    }

    public static void savePcmModel(Common c, Repository repository) {
        PcmUtils.saveModel(Common.DataRootPath + "results/" + c.logMode + "/result.repository", repository);
    }

    @BeforeClass
    public static void setUp() {
        LoggingUtil.InitConsoleLogger();
    }    
    
    @Test
    public void evaluation() throws Exception {
        evaluationStep(Common.EvaluationData.Threaded2);
        //evaluationStep(Common.EvaluationData.Default);
        //evaluationStep(Common.EvaluationData.Threaded);
        //evaluationStep(Common.EvaluationData.Threaded2);
    }

    public static void evaluationStep(String name) throws Exception {
        Common c;
        
        // iteration 0
        c = new Common(Mode.Iteration0, Mode.Iteration0, name);
        SeffParameterEstimation estimation = new SeffParameterEstimation();

        MonitoringDataSet readerIteration0 = getReader(c);
        Repository pcmModelIteration0 = loadPcmModel(c);

        estimation.update(pcmModelIteration0, readerIteration0);

        savePcmModel(c, pcmModelIteration0);
        storeUtilization(c, readerIteration0);
        
        // iteration 1
        c = new Common(Mode.Iteration1, Mode.Iteration1, name);
        MonitoringDataSet readerIteration1 = getReader(c);
        Repository pcmModelIteration1 = loadPcmModel(c);

        estimation.update(pcmModelIteration1, readerIteration1);

        savePcmModel(c, pcmModelIteration1);
        storeUtilization(c, readerIteration1);
        
        // complete
        c = new Common(Mode.Complete, Mode.Iteration1, name);
        SeffParameterEstimation estimationComplete = new SeffParameterEstimation();

        MonitoringDataSet readerComplete = getReader(c);
        Repository pcmModelComplete = loadPcmModel(c);

        estimationComplete.update(pcmModelComplete, readerComplete);

        savePcmModel(c, pcmModelComplete);
        storeUtilization(c, readerComplete);
    }

    public void compareMonitoringRecordsCount(String name) throws Exception {
        Common d;
        
        d = new Common(Mode.Iteration0, Mode.Iteration0, name);
        int a = getMonitoringRecordsCount(getReader(d));

        d = new Common(Mode.Iteration1, Mode.Iteration1, name);
        int b = getMonitoringRecordsCount(getReader(d));

        d = new Common(Mode.Complete, Mode.Iteration1, name);
        int c = getMonitoringRecordsCount(getReader(d));

        System.out.println("a " + String.valueOf(a) + " b " + String.valueOf(b) + " c " + String.valueOf(c));

    }
    
    private static void storeUtilization(Common c, MonitoringDataSet monitoringData)
            throws IOException {
        Long earliest = monitoringData.getResponseTimes().getEarliestEntry();
        Long last = monitoringData.getResponseTimes().getLatestEntry();

        String resourceId = monitoringData.getResourceUtilizations().getResourceIds().iterator().next();
        SortedMap<Long, Double> utilization = monitoringData.getResourceUtilizations().getUtilization(resourceId);

        CsvWriter csv2 = new CsvWriter(c.getUtilizationResultPath() + "utilization.csv", "time", "utilization");

        for (Entry<Long, Double> utilizationEntry : utilization.subMap(earliest, last).entrySet()) {
            csv2.write(monitoringData.getResourceUtilizations().timeToSeconds(utilizationEntry.getKey()),
                    utilizationEntry.getValue());
        }
        csv2.close();
    }

    private static int getMonitoringRecordsCount(MonitoringDataSet dataSet) {
        int count = 0;
        for (String branchId : dataSet.getBranches().getBranchIds()) {
            count += dataSet.getBranches().getBranchRecords(branchId).size();
        }

        for (String branchId : dataSet.getLoops().getLoopIds()) {
            count += dataSet.getLoops().getLoopRecords(branchId).size();
        }

        for (String branchId : dataSet.getResponseTimes().getInternalActionIds()) {
            for (String b : dataSet.getResponseTimes().getResourceIds(branchId)) {
                count += dataSet.getResponseTimes().getResponseTimes(branchId, b).size();
            }
        }

        count += dataSet.getServiceCalls().getServiceCalls().size();
        return count;
    }
}
