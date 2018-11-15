package evaluation.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.LoopRecord;

public class EvaluationLoopDataSet implements LoopDataSet {
    private final Map<String, List<LoopRecord>> loopIdToRecord;

    public EvaluationLoopDataSet() {
        this.loopIdToRecord = new HashMap<>();
    }

    public void add(String serviceExecutionId, long iterations) {
        LoopRecord record = new LoopRecord(Common.DEFAULT_SESSION_ID, serviceExecutionId, Common.DEFAULT_MODEL_ID,
                iterations);

        String loopId = record.getLoopId();
        List<LoopRecord> loopRecords = this.loopIdToRecord.get(loopId);
        if (loopRecords == null) {
            loopRecords = new ArrayList<>();
            this.loopIdToRecord.put(loopId, loopRecords);
        }
        loopRecords.add(record);
    }

    @Override
    public Set<String> getLoopIds() {
        return this.loopIdToRecord.keySet();
    }

    @Override
    public List<LoopRecord> getLoopRecords(final String loopId) {
        return this.loopIdToRecord.get(loopId);
    }
}
