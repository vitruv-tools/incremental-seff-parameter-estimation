package tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.dataset;

import java.util.Map;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.ResponseTimeDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.utilization.ResourceUtilizationDataSet;

public class MockMonitoringDataSet implements MonitoringDataSet {

    private final MockLoopDataSet loopDataSet;
    private final MockBranchDataSet branchDataSet;

    private final MockServiceCallDataSet serviceCallDataSet;

    public MockMonitoringDataSet() {
        this.loopDataSet = new MockLoopDataSet();
        this.branchDataSet = new MockBranchDataSet();
        this.serviceCallDataSet = new MockServiceCallDataSet();
    }

    public void addLoop(Map<String, Object> serviceArguments, long iterations) {
        String executionId = this.serviceCallDataSet.add(serviceArguments);
        this.loopDataSet.add(executionId, iterations);
    }

    public void addBranch(Map<String, Object> serviceArguments, String transition) {
        String executionId = this.serviceCallDataSet.add(serviceArguments);
        this.branchDataSet.add(executionId, transition);
    }

    @Override
    public BranchDataSet getBranches() {
        return this.branchDataSet;
    }

    @Override
    public LoopDataSet getLoops() {
        return this.loopDataSet;
    }

    @Override
    public ResourceUtilizationDataSet getResourceUtilizations() {
        return null;
    }

    @Override
    public ResponseTimeDataSet getResponseTimes() {
        return null;
    }

    @Override
    public ServiceCallDataSet getServiceCalls() {
        return this.serviceCallDataSet;
    }

}
