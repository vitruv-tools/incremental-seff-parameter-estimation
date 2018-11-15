package evaluation.dataset;

import java.util.Map;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.ResponseTimeDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.utilization.ResourceUtilizationDataSet;

public class EvaluationMonitoringDataSet implements MonitoringDataSet {

    private final EvaluationLoopDataSet loopDataSet;
    private final EvaluationBranchDataSet branchDataSet;

    private final EvaluationServiceCallDataSet serviceCallDataSet;

    public EvaluationMonitoringDataSet() {
        this.loopDataSet = new EvaluationLoopDataSet();
        this.branchDataSet = new EvaluationBranchDataSet();
        this.serviceCallDataSet = new EvaluationServiceCallDataSet();
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseTimeDataSet getResponseTimes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceCallDataSet getServiceCalls() {
        return this.serviceCallDataSet;
    }

}
