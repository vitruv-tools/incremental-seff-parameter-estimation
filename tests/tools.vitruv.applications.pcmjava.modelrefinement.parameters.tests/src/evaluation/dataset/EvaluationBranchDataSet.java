package evaluation.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.BranchDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.BranchRecord;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.LoopRecord;

public class EvaluationBranchDataSet implements BranchDataSet {
    private final Map<String, List<BranchRecord>> branchIdToRecord;

    public EvaluationBranchDataSet() {
        this.branchIdToRecord = new HashMap<>();
    }

    public void add(String serviceExecutionId, String executedBranchId) {
        String nonNullExecutedBranchId = executedBranchId == null ? BranchRecord.EXECUTED_BRANCH_ID : executedBranchId;
        BranchRecord record = new BranchRecord(Common.DEFAULT_SESSION_ID, serviceExecutionId, Common.DEFAULT_MODEL_ID,
                nonNullExecutedBranchId);

        String branchId = record.getBranchId();
        List<BranchRecord> branchRecords = this.branchIdToRecord.get(branchId);
        if (branchRecords == null) {
            branchRecords = new ArrayList<>();
            this.branchIdToRecord.put(branchId, branchRecords);
        }
        branchRecords.add(record);
    }

    @Override
    public Set<String> getBranchIds() {
        return this.branchIdToRecord.keySet();
    }

    @Override
    public String getBranchNotExecutedId() {
        return BranchRecord.EXECUTED_BRANCH_ID;
    }

    @Override
    public List<BranchRecord> getBranchRecords(final String branchId) {
        return this.branchIdToRecord.get(branchId);
    }
}
