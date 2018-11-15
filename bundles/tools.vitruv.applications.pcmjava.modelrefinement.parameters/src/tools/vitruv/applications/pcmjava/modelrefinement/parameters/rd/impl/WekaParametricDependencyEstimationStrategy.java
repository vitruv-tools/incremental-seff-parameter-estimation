package tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModelMode;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Implements the resource demand parametric dependency estimation by using linear regression from the weka library.
 * This does not imply that only linear dependencies can be detected, because we present different pre-defined possible
 * dependency relations, such as a quadratic dependency, as input. The linear regression then finds the best candidates.
 * 
 * @author JP
 *
 */
public class WekaParametricDependencyEstimationStrategy implements ParametricDependencyEstimationStrategy {

    @Override
    public ResourceDemandModel estimateResourceDemandModel(final String internalActionId, final String resourceId,
            final Map<ServiceParameters, Double> resourceDemands) {
        try {
            return this.internEstimateResourceDemandModel(internalActionId, resourceId, resourceDemands);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceDemandModel internEstimateResourceDemandModel(final String internalActionId,
            final String resourceId,
            final Map<ServiceParameters, Double> resourceDemands) throws Exception {

        // If no service parameters are monitored, we have a constant resource demand.
        if (resourceDemands.size() == 1) {
            double singleValue = resourceDemands.values().iterator().next();
            return new ConstantResourceDemandModel(singleValue);
        }

        ServiceParameters prototypeParameters = resourceDemands.keySet().iterator().next();

        Attribute classAttribute = new Attribute("resourceDemand");
        WekaServiceParametersModel parametersConversion = new WekaServiceParametersModel(prototypeParameters,
                classAttribute, WekaServiceParametersModelMode.NumericOnly);
        Instances dataset = parametersConversion.buildDataSet();

        for (Entry<ServiceParameters, Double> rdEntry : resourceDemands.entrySet()) {
            Instance dataPoint = parametersConversion.buildInstance(rdEntry.getKey(), rdEntry.getValue());
            dataset.add(dataPoint);
        }

        return new WekaResourceDemandModel(dataset, parametersConversion);
    }

    private static class ConstantResourceDemandModel implements ResourceDemandModel {

        private final double resourceDemand;

        public ConstantResourceDemandModel(final double resourceDemand) {
            this.resourceDemand = resourceDemand;
        }

        @Override
        public double predictResourceDemand(final ServiceCall serviceCall) {
            return this.resourceDemand;
        }

        @Override
        public String getResourceDemandStochasticExpression() {
            return String.valueOf(this.resourceDemand);
        }
    }

}
