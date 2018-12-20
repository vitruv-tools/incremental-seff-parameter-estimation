package evaluation.casestudy;

import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.SeffParameterEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.impl.KiekerMonitoringReader;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class SeffParameterEstimationTest {

    public static KiekerMonitoringReader getReader(final Mode logMode, Mode runMode, boolean threaded) {
        String thing = threaded ? "threaded" : "default";
        return new KiekerMonitoringReader(Common.DataRootPath + "kieker-logs/" + thing,
                Common.getSessionId(logMode, runMode));
    }

    public static Repository loadPcmModel(Mode runMode) {
        return PcmUtils.readFromFile(Common.DataRootPath + runMode + "-model/default.repository", Repository.class);
    }

    public static void savePcmModel(Mode runMode, Repository repository) {
        PcmUtils.saveModel(Common.DataRootPath + "/results/" + runMode + "/result.repository", repository);
    }

    public static final boolean Threaded = false;

    @Test
    public void iterationEvaluationTest() throws Exception {
        SeffParameterEstimation estimation = new SeffParameterEstimation();

        MonitoringDataSet readerIteration0 = getReader(Mode.Iteration0, Mode.Iteration0, Threaded);
        Repository pcmModelIteration0 = loadPcmModel(Mode.Iteration0);

        estimation.update(pcmModelIteration0, readerIteration0);

        savePcmModel(Mode.Iteration0, pcmModelIteration0);

        MonitoringDataSet readerIteration1 = getReader(Mode.Iteration1, Mode.Iteration1, Threaded);
        Repository pcmModelIteration1 = loadPcmModel(Mode.Iteration1);

        estimation.update(pcmModelIteration1, readerIteration1);

        savePcmModel(Mode.Iteration1, pcmModelIteration1);
    }

    @Test
    public void completeEvaluationTest() throws Exception {
        SeffParameterEstimation estimation = new SeffParameterEstimation();

        MonitoringDataSet reader = getReader(Mode.Complete, Mode.Iteration1, Threaded);
        Repository pcmModel = loadPcmModel(Mode.Iteration1);

        estimation.update(pcmModel, reader);

        savePcmModel(Mode.Complete, pcmModel);
    }

    @Test
    public void compareMonitoringRecordsCount() throws Exception {

        MonitoringDataSet readerIteration0 = getReader(Mode.Iteration0, Mode.Iteration0, Threaded);
        int a = getMonitoringRecordsCount(readerIteration0);

        MonitoringDataSet readerIteration1 = getReader(Mode.Iteration1, Mode.Iteration1, Threaded);
        int b = getMonitoringRecordsCount(readerIteration1);

        MonitoringDataSet readerComplete = getReader(Mode.Complete, Mode.Iteration1, Threaded);
        int c = getMonitoringRecordsCount(readerComplete);

        System.out.println("a " + String.valueOf(a) + " b " + String.valueOf(b) + " c " + String.valueOf(c));

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

    @BeforeClass
    public static void setUp() {
        LoggingUtil.InitConsoleLogger();
    }
}
