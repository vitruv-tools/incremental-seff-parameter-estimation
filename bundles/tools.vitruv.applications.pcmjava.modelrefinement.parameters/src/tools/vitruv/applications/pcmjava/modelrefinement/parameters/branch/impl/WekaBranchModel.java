package tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl;

import java.util.Optional;
import java.util.Random;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.WekaServiceParametersModel;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.j48.C45Split;
import weka.classifiers.trees.j48.ClassifierSplitModel;
import weka.classifiers.trees.j48.ClassifierTree;
import weka.classifiers.trees.j48.NoSplit;
import weka.core.Instance;
import weka.core.Instances;

public class WekaBranchModel implements BranchModel {
    private final WekaBranchModel.StochasticExpressionJ48 classifier;
    private final WekaServiceParametersModel parametersModel;
    private final Random random;
    private final String branchNotExecutedId;
    private final String[] attributeExpressions;
    
    private final Instances dataset;

    public WekaBranchModel(final Instances dataset,
            final WekaServiceParametersModel parametersConversion,
            final Random random, final String branchNotExecutedId) throws Exception {
        this.dataset = dataset;
        this.parametersModel = parametersConversion;
        WekaBranchModel.StochasticExpressionJ48 tree = new StochasticExpressionJ48();
        tree.buildClassifier(dataset);
        this.classifier = tree;
        this.random = random;
        this.branchNotExecutedId = branchNotExecutedId;
        this.attributeExpressions = new String[parametersConversion.getInputAttributesCount()];
        for (int i = 0; i < this.attributeExpressions.length; i++) {
            this.attributeExpressions[i] = parametersConversion.getStochasticExpressionForIndex(i);
        }
    }

    public WekaBranchModel.StochasticExpressionJ48 getClassifier() {
        return classifier;
    }

    public Instances getDataset() {
        return dataset;
    }

    @Override
    public Optional<String> predictBranchId(final ServiceCall serviceCall) {
        Instance parametersInstance = this.parametersModel.buildInstance(serviceCall.getParameters(), 0);
        Instances dataset = this.parametersModel.buildDataSet();
        dataset.add(parametersInstance);
        double[] branchDistribution;
        try {
            branchDistribution = this.classifier.distributionForInstance(dataset.firstInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        double selectedBranchPropability = this.random.nextDouble();
        int selectedBranchIndex = 0;
        double branchPropabilitySum = 0.0;
        while (true) {
            if (selectedBranchIndex >= branchDistribution.length) {
                throw new IllegalArgumentException("The branch has propability distribution.");
            }
            branchPropabilitySum += branchDistribution[selectedBranchIndex];
            if (selectedBranchPropability < branchPropabilitySum) {
                break;
            }
            selectedBranchIndex++;
        }

        String result = this.parametersModel.getClassAttribute().value(selectedBranchIndex);

        if (result.equals(this.branchNotExecutedId)) {
            return Optional.empty();
        } else {
            return Optional.of(result);
        }
    }

    @Override
    public String getBranchStochasticExpression(final String transitionId) {
        return this.classifier.getBranchStochasticExpression(0, this.attributeExpressions);
    }
    
    public static class StochasticExpressionJ48 extends J48 {

        /** for serialization */
        private static final long serialVersionUID = -8479361310735737366L;

        public String getBranchStochasticExpression(final int classId, final String[] attributeExpression) {
            StringBuilder result = new StringBuilder();
            this.buildStochasticExpression(this.m_root, classId, attributeExpression, result);
            return result.toString();
        }

        private void buildStochasticExpression(final ClassifierTree tree, final int classId,
                final String[] attributeExpression,
                final StringBuilder result2) {

            // "BoolPMF[(true;p)(false;q)]"

            if (tree.isLeaf()) {
                int maxClass = tree.getLocalModel().distribution().maxClass(0);
                if (maxClass == classId) {
                    result2.append("true");
                } else {
                    result2.append("false");
                }
            } else {
                String opposite = null;
                for (int i = 0; i < tree.getSons().length; i++) {
                    result2.append("(");
                    result2.append(
                            this.toSourceExpression(tree.getLocalModel(), i, attributeExpression,
                                    tree.getTrainingData()))
                            .append(" ? ");

                    if (tree.getSons()[i].isLeaf()) {
                        int maxClass = tree.getLocalModel().distribution().maxClass(i);
                        if (maxClass == classId) {
                            result2.append("true");
                            opposite = "false";
                        } else {
                            result2.append("false");
                            opposite = "true";
                        }
                    } else {
                        result2.append("( ");
                        this.buildStochasticExpression(tree.getSons()[i], classId, attributeExpression, result2);
                        result2.append(" )");
                    }
                    result2.append(" : ");
                }
                result2.append(opposite).append(" ");
                for (int i = 0; i < tree.getSons().length; i++) {
                    result2.append(")");
                }
            }
        }

        private String toSourceExpression(final C45Split splitModel, final int index,
                final String[] attributeExpression,
                final Instances data) {
            StringBuffer expr = new StringBuffer();
            expr.append(attributeExpression[splitModel.attIndex()]);

            if (data.attribute(splitModel.attIndex()).isNominal()) {
                expr.append(" == ").append(data.attribute(splitModel.attIndex()).value(index)).append("\")");
            } else {
                if (index == 0) {
                    expr.append(" <= ").append(splitModel.splitPoint());
                } else {
                    expr.append(" > ").append(splitModel.splitPoint());
                }
            }
            return expr.toString();
        }

        private String toSourceExpression(final ClassifierSplitModel splitModel, final int index,
                final String[] attributeExpression,
                final Instances data) {
            if (splitModel instanceof NoSplit) {
                return "true";
            } else if (splitModel instanceof C45Split) {
                return this.toSourceExpression((C45Split) splitModel, index, attributeExpression, data);
            }
            throw new UnsupportedOperationException();
        }
    }
}