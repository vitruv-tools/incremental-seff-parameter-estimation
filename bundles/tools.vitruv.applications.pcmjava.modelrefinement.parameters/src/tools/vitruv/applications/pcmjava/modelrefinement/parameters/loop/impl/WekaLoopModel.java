package tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.impl;

import java.util.StringJoiner;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModel;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

public class WekaLoopModel implements LoopModel {

    private final LinearRegression classifier;

    private final WekaServiceParametersModel parametersConversion;

    private final Instances dataset;

    public WekaLoopModel(final Instances dataset, final WekaServiceParametersModel parametersConversion)
            throws Exception {
        this.dataset = dataset;
        this.parametersConversion = parametersConversion;

        this.classifier = new LinearRegression();
        this.classifier.buildClassifier(this.dataset);
    }

    public LinearRegression getClassifier() {
        return classifier;
    }

    public Instances getDataset() {
        return dataset;
    }

    @Override
    public double predictIterations(final ServiceCall serviceCall) {
        Instance parametersInstance = this.parametersConversion.buildInstance(serviceCall.getParameters(), 0);
        try {
            return this.classifier.classifyInstance(parametersInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getIterationsStochasticExpression() {
        StringJoiner result = new StringJoiner(" + (");
        double[] coefficients = this.classifier.coefficients();
        int braces = 0;
        for (int i = 0; i < coefficients.length - 2; i++) {
            int coefficient = round(coefficients[i]);
            if (coefficient == 0) {
                continue;
            }
            StringBuilder coefficientPart = new StringBuilder();
            String paramStoEx = this.parametersConversion.getStochasticExpressionForIndex(i);
            coefficientPart.append(coefficient).append(" * ").append(paramStoEx);
            result.add(coefficientPart.toString());
            braces++;
        }
        result.add(String.valueOf(round(coefficients[coefficients.length - 1])));
        StringBuilder strBuilder = new StringBuilder().append(result.toString());
        for (int i = 0; i < braces; i++) {
            strBuilder.append(")");
        }
        return strBuilder.toString();
    }

    private static int round(final double value) {
        return (int) Math.round(value);
    }
}