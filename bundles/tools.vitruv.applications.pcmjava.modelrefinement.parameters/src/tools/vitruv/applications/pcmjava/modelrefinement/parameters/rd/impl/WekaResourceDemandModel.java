package tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl;

import java.util.StringJoiner;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModel;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;

public class WekaResourceDemandModel implements ResourceDemandModel {

    private final LinearRegression classifier;
    private final WekaServiceParametersModel parametersConversion;
    private final Instances dataset;

    public WekaResourceDemandModel(final Instances dataset,
            final WekaServiceParametersModel parametersConversion) throws Exception {
        this.dataset = dataset;
        this.parametersConversion = parametersConversion;
        
        this.classifier = new LinearRegression();
        this.classifier.buildClassifier(dataset);
    }

    
    public LinearRegression getClassifier() {
        return classifier;
    }


    public Instances getDataset() {
        return dataset;
    }


    @Override
    public double predictResourceDemand(final ServiceCall serviceCall) {
        Instance parametersInstance = this.parametersConversion.buildInstance(serviceCall.getParameters(), 0);
        try {
            return this.classifier.classifyInstance(parametersInstance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getResourceDemandStochasticExpression() {
        StringJoiner result = new StringJoiner(" + (");
        double[] coefficients = this.classifier.coefficients();
        int braces = 0;
        for (int i = 0; i < coefficients.length - 2; i++) {
            if (coefficients[i] == 0.0) {
                continue;
            }
            StringBuilder coefficientPart = new StringBuilder();
            String paramStoEx = this.parametersConversion.getStochasticExpressionForIndex(i);
            coefficientPart.append(coefficients[i]).append(" * ").append(paramStoEx);
            result.add(coefficientPart.toString());
            braces++;
        }
        result.add(String.valueOf(coefficients[coefficients.length - 1]));
        StringBuilder strBuilder = new StringBuilder().append(result.toString());
        for (int i = 0; i < braces; i++) {
            strBuilder.append(")");
        }
        return strBuilder.toString();
    }
}