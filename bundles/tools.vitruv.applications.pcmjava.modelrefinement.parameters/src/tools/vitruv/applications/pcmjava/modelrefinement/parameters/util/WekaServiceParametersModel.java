package tools.vitruv.applications.pcmjava.modelrefinement.parameters.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * A parameters model, which defines different possible parametric dependency relations.
 * 
 * @author JP
 *
 */
public class WekaServiceParametersModel {

    private static final Logger LOGGER = Logger.getLogger(WekaServiceParametersModel.class);

    private final Attribute classAttribute;

    private final ArrayList<Attribute> attributes;

    private final List<WekaServiceParameter> parameters;

    private final Map<String, List<WekaServiceParameter>> parametersToAttributes;
    
    private final WekaServiceParametersModelMode mode;

    /**
     * Initializes a new instance of {@link WekaServiceParametersModel}.
     * 
     * @param basedOnParameters
     *            These parameters are used to build the weka attribute.
     * @param classAttribute
     *            The class attribute for the weka data set.
     */
    public WekaServiceParametersModel(final ServiceParameters basedOnParameters, final Attribute classAttribute, final WekaServiceParametersModelMode mode) {
        this.attributes = new ArrayList<>();
        this.parameters = new ArrayList<>();
        this.parametersToAttributes = new HashMap<>();
        this.mode = mode;
        
        for (Entry<String, Object> parameter : basedOnParameters.getParameters().entrySet()) {
            this.addParameter(parameter.getKey(), parameter.getValue());
        }

        this.attributes.add(classAttribute);
        this.classAttribute = classAttribute;
    }

    /**
     * Gets the weka data set.
     * 
     * @return Weka data set.
     */
    public Instances buildDataSet() {
        Instances instances = new Instances("dataset", this.attributes, 0);
        instances.setClass(this.classAttribute);
        return instances;
    }

    /**
     * Creates a weka data instance from service parameters and the class value.
     * 
     * @param serviceParameters
     *            The service parameters for the data instance.
     * @param classValue
     *            The class value for the data instance.
     * @return A weka data instance.
     */
    public Instance buildInstance(final ServiceParameters serviceParameters, final double classValue) {
        return this.buildInstance(serviceParameters, classValue, 1.0);
    }
    
    public Instance buildInstance(final ServiceParameters serviceParameters, final double classValue, final double weight) {
        double[] values = new double[this.parameters.size() + 1];

        for (Entry<String, Object> parameter : serviceParameters.getParameters().entrySet()) {
            List<WekaServiceParameter> wekaParameters = this.parametersToAttributes.get(parameter.getKey());
            if (wekaParameters != null) {
                for (WekaServiceParameter wekaServiceParameter : wekaParameters) {
                    wekaServiceParameter.setValue(parameter.getValue(), values);
                }
            }
        }

        values[this.parameters.size()] = classValue;
        return new DenseInstance(weight, values);
    }

    /**
     * Gets all weka attributes, including the class attribute.
     * 
     * @return all weka attributes.
     */
    public ArrayList<Attribute> getAttributes() {
        return this.attributes;
    }

    /**
     * Gets the weka class attribute.
     * 
     * @return The weka class attribute.
     */
    public Attribute getClassAttribute() {
        return this.classAttribute;
    }

    /**
     * Gets the number of attributes, without the class attribute.
     * 
     * @return The number of input attributes.
     */
    public int getInputAttributesCount() {
        return this.attributes.size() - 1;
    }

    /**
     * Gets the stochastic expression for a attribute index. For example the string "a ^ 2" is returned.
     * 
     * @param idx
     *            The attribute index.
     * @return The Stochastic Expression for the attribute as string.
     */
    public String getStochasticExpressionForIndex(final int idx) {
        return this.parameters.get(idx).getStochasticExpression();
    }

    private void addNumericParameter(final String name) {
        List<WekaServiceParameter> newParameters = new ArrayList<>();
        int index = this.parameters.size();
        newParameters.add(new NumericWekaServiceParameter(name, index));
        index++;
        
        if (this.mode != WekaServiceParametersModelMode.NoTransformations) {
            newParameters.add(new PowerNumericWekaServiceParameter(name, index, 2.0));
            index++;
            newParameters.add(new PowerNumericWekaServiceParameter(name, index, 3.0));
            index++;
            
            if (this.mode != WekaServiceParametersModelMode.IntegerOnly) {
                newParameters.add(new PowerNumericWekaServiceParameter(name, index, 0.5));
                index++;
            }
        }
        
        for (WekaServiceParameter wekaServiceParameter : newParameters) {
            this.parameters.add(wekaServiceParameter);
            this.attributes.add(wekaServiceParameter.getWekaAttribute());
        }
       
        this.parametersToAttributes.put(name, newParameters);
    }
    
    private void addStringParameter(final String name) {
        int index = this.parameters.size();
        WekaServiceParameter newParameter = new StringWekaServiceParameter(name, index);
        
        this.parameters.add(newParameter);
        this.attributes.add(newParameter.getWekaAttribute());
        this.parametersToAttributes.put(name, Collections.singletonList(newParameter));
    }
    
    private void addBooleanParameter(final String name) {
        int index = this.parameters.size();
        WekaServiceParameter newParameter = new BooleanWekaServiceParameter(name, index);
        
        this.parameters.add(newParameter);
        this.attributes.add(newParameter.getWekaAttribute());
        this.parametersToAttributes.put(name, Collections.singletonList(newParameter));
    }

    private void addParameter(final String name, final Object value) {
        if (value instanceof Integer ) {
            this.addNumericParameter(name);
        } else if (value instanceof Double && this.mode != WekaServiceParametersModelMode.IntegerOnly) {
            this.addNumericParameter(name);
        } else if (value instanceof Boolean && this.mode != WekaServiceParametersModelMode.IntegerOnly && this.mode != WekaServiceParametersModelMode.NumericOnly) {
            this.addBooleanParameter(name);
        } else if (value instanceof Double && this.mode != WekaServiceParametersModelMode.IntegerOnly && this.mode != WekaServiceParametersModelMode.NumericOnly) {
            this.addStringParameter(name);
        } else {
            LOGGER.warn("Handling parameter of type " + value.getClass().getSimpleName() + " is not implemented.");
        }
    }
    
    private static class BooleanWekaServiceParameter extends WekaServiceParameter {

        public BooleanWekaServiceParameter(final String parameterName, final int index) {
            super(parameterName, index, new Attribute(parameterName, createPossibleValues()));
        }
        
        private static ArrayList<String> createPossibleValues() {
            // TODO: Bad hack
            ArrayList<String> possibleValues = new ArrayList<String>();
            possibleValues.add(String.valueOf(true));
            possibleValues.add(String.valueOf(false));
            return possibleValues;
        }

        @Override
        public void setValue(final Object value, final double[] result) {
            String stringValue = String.valueOf((Boolean) value);
            result[this.getIndex()] = this.getWekaAttribute().indexOfValue(stringValue);
        }
    }
    
    private static class StringWekaServiceParameter extends WekaServiceParameter {

        public StringWekaServiceParameter(final String parameterName, final int index) {
            super(parameterName, index, new Attribute(parameterName, createPossibleValues()));
        }
        
        private static ArrayList<String> createPossibleValues() {
            // TODO: Bad hack
            ArrayList<String> possibleValues = new ArrayList<String>();
            possibleValues.add("aaa");
            possibleValues.add("aab");
            possibleValues.add("aac");
            possibleValues.add("aad");
            possibleValues.add("aae");
            possibleValues.add("aaf");
            possibleValues.add("aag");
            possibleValues.add("aah");
            possibleValues.add("aai");
            possibleValues.add("aaj");
            return possibleValues;
        }

        @Override
        public void setValue(final Object value, final double[] result) {
            result[this.getIndex()] = this.getWekaAttribute().indexOfValue((String) value);
        }
    }

    private static class NumericWekaServiceParameter extends WekaServiceParameter {

        public NumericWekaServiceParameter(final String parameterName, final int index) {
            super(parameterName, index, new Attribute(parameterName));
        }

        @Override
        public void setValue(final Object value, final double[] result) {
            double castedValue = 0.0;
            if (value instanceof Integer) {
                castedValue = (Integer) value;
            } else if (value instanceof Double) {
                castedValue = (Double) value;
            }

            result[this.getIndex()] = castedValue;
        }
    }

    private static class PowerNumericWekaServiceParameter extends WekaServiceParameter {

        private final double power;

        public PowerNumericWekaServiceParameter(final String parameterName, final int index, final double power) {
            super(parameterName, index, new Attribute(parameterName + "-pow-" + String.valueOf(power)));
            this.power = power;
        }

        @Override
        public String getStochasticExpression() {
            return "(" + this.getParameterName() + " ^ " + String.valueOf(this.power) + ")";
        }

        @Override
        public void setValue(final Object value, final double[] result) {
            double castedValue = 0.0;
            if (value instanceof Integer) {
                castedValue = (Integer) value;
            } else if (value instanceof Double) {
                castedValue = (Double) value;
            }

            result[this.getIndex()] = Math.pow(castedValue, this.power);
        }

    }

    private static abstract class WekaServiceParameter {
        private final int index;
        private final String parameterName;
        private final Attribute wekaAttribute;

        public WekaServiceParameter(final String parameterName, final int index, final Attribute wekaAttribute) {
            this.index = index;
            this.parameterName = parameterName;
            this.wekaAttribute = wekaAttribute;
        }

        public int getIndex() {
            return this.index;
        }

        public String getParameterName() {
            return this.parameterName;
        }

        public String getStochasticExpression() {
            return this.getParameterName();
        }

        public Attribute getWekaAttribute() {
            return this.wekaAttribute;
        }

        public abstract void setValue(Object value, double[] result);
    }
}
