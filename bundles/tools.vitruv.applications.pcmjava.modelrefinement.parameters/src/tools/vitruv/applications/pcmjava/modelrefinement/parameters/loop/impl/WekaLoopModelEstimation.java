package tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.LoopDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.LoopRecord;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModelMode;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;

/**
 * Implements the loop model estimation by using linear regression from the weka library. This does not imply that only
 * linear dependencies can be detected, because we present different pre-defined possible dependency relations, such as
 * a quadratic dependency, as input. The linear regression then finds the best candidates.
 * 
 * @author JP
 *
 */
public class WekaLoopModelEstimation {

    private final ServiceCallDataSet serviceCalls;

    private final LoopDataSet loopIterations;

    /**
     * Initializes a new instance of {@link WekaLoopModelEstimation}.
     * 
     * @param serviceCalls
     *            The service call data set.
     * @param loopIterations
     *            The loop record data set.
     */
    public WekaLoopModelEstimation(final ServiceCallDataSet serviceCalls,
            final LoopDataSet loopIterations) {
        this.serviceCalls = serviceCalls;
        this.loopIterations = loopIterations;
    }

    /**
     * Gets a loop model for a specific loop id.
     * 
     * @param loopId
     *            The id of the loop the model is build for.
     * @return The estimated loop model.
     */
    public LoopModel estimate(final String loopId) {
        try {
            return this.internEstimate(loopId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets for each loop in the {@link LoopDataSet} a loop model.
     * 
     * @return A map, which maps loop IDs to their corresponding loop model.
     */
    public Map<String, LoopModel> estimateAll() {
        HashMap<String, LoopModel> returnValue = new HashMap<>();
        for (String loopId : this.loopIterations.getLoopIds()) {
            returnValue.put(loopId, this.estimate(loopId));
        }
        return returnValue;
    }

    private LoopModel internEstimate(final String loopId) throws Exception {
        List<LoopRecord> records = this.loopIterations.getLoopRecords(loopId);

        if (records.size() == 0) {
            throw new IllegalStateException("No records for loop id " + loopId + " found.");
        }

        LoopRecord firstRecord = records.get(0);

        Attribute loopIterations = new Attribute("loopIterations");

        WekaDataSet dataSetBuilder = new WekaDataSet(
                this.serviceCalls,
                firstRecord.getServiceExecutionId(),
                loopIterations,
                WekaServiceParametersModelMode.IntegerOnly);

        for (LoopRecord record : records) {
            dataSetBuilder.addInstance(record.getServiceExecutionId(), record.getLoopIterationCount());
        }

        Instances dataset = dataSetBuilder.getDataSet();
        return new WekaLoopModel(dataset, dataSetBuilder.getServiceParametersModel());
    }
}
