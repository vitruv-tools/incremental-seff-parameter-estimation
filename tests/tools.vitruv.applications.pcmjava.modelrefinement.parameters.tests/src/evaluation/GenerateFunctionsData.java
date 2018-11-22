package evaluation;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Random;
import java.util.Spliterator.OfDouble;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.LongSupplier;

import org.apache.commons.lang3.RandomStringUtils;

import evaluation.dataset.Common;
import evaluation.dataset.EvaluationMonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl.BranchModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl.TreeWekaBranchModelEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl.WekaBranchModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.impl.LoopEstimationImpl;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.impl.WekaLoopModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.loop.impl.WekaLoopModelEstimation;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.records.ServiceCallRecord;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl.ResourceDemandModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl.WekaParametricDependencyEstimationStrategy;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.rd.impl.WekaResourceDemandModel;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class GenerateFunctionsData {

    private static final int CONST_INT = 1;
    private static final String CONST_INT_NAME = "ConstInt";
    private static final String CONST_STRING = "Const String";
    private static final String CONST_STRING_NAME = "ConstString";
    private static final double CONST_DECIMAL = 2.0;
    private static final String CONST_DECIMAL_NAME = "ConstDecimal";
    private static final boolean CONST_BOOLEAN = false;
    private static final String CONST_BOOLEAN_NAME = "ConstBoolean";

    private static final String RANDOM_INT_NAME = "RandomInt";
    private static final String RANDOM_STRING_NAME = "RandomString";
    private static final int RANDOM_STRING_LENGTH = 3;
    private static final String RANDOM_DECIMAL_NAME = "RandomDecimal";
    private static final String RANDOM_BOOLEAN_NAME = "RandomBoolean";

    private static final String DEPENDENT_NAME = "Dependent";
    private static final int DEPENDENT_CONST_INT = 10;
    private static final double DEPENDENT_CONST_DOUBLE = 0.23;
    private static final String MISLEAD_DEPENDENT_NAME = "MisleadDependent";

    private static final String BRANCH_TRANSITION_ID1 = "Transition1";
    private static final String BRANCH_TRANSITION_ID2 = "Transition2";

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

    private static final ArrayList<String> PossibleStringValues = createPossibleValues();

    private static ArrayList<Integer> createPossibleImageTypeValues() {
        ArrayList<Integer> possibleValues = new ArrayList<Integer>();
        possibleValues.add(BufferedImage.TYPE_INT_RGB);
        possibleValues.add(BufferedImage.TYPE_4BYTE_ABGR);
        possibleValues.add(BufferedImage.TYPE_BYTE_BINARY);
        return possibleValues;
    }

    private static final ArrayList<Integer> PossibleImageTypeValues = createPossibleImageTypeValues();

    private static ImageScaleFunction imageScaleFunction;
    private static FunctionDataSet imageScaleFunctionDataSet;

    public static void main(final String[] args) throws Exception {

        System.out.println("Set processor affinity to CPU 0 and press enter.");
        System.in.read();

        //imageScaleFunction = new ImageScaleFunction();
        //imageScaleFunctionDataSet = imageScaleFunction.generateImageScale(100);
        
        loopEval();
        //branchEval();
        // rdEval();
        //argEval();
    }

    private static void argEval() throws Exception {
        Random random = new Random(10);
        int dataAmount = 100;

        int trueCount = 0;
        // boolean identity dependency relation
        for (int i = 0; i < dataAmount; i++) {
            if (random.nextBoolean()) {
                trueCount++;
            }
        }
        System.out.println(trueCount);
        
        int trueCount2 = 0;
        // boolean identity dependency relation
        for (int i = 0; i < dataAmount; i++) {
            if (random.nextBoolean()) {
                trueCount++;
            }
            
        }
        System.out.println(trueCount);
        
        // int identity dependency relation

        // string identity dependency relation

        // Complex fibonacci dependency relation
        OfInt randomf = random.ints(100, 10000).iterator();
        int input = randomf.nextInt();
        long time = average(() -> measureTime(() -> fibonacci(input)), 100);

        // Complex scale dependency relation
        /*branchEvaluation(imageScaleFunctionDataSet.createDataSet((points, dataPoint, dataSet) -> {
            dataSet.addBranch(dataPoint.arguments,
                    dataPoint.responseTime < points.firstFifth ? BRANCH_TRANSITION_ID1 : null);
        }), "./test-data/dependencies/branch-tree.txt");*/

        // independent random
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> random.nextBoolean(), dataAmount));

        // independent constant
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> true, 100));
    }

    private static void branchEval() throws Exception {
        Random random = new Random(0);
        int dataAmount = 100;

        System.out.println("boolean identity dependency relation");
        // boolean identity dependency relation
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> {
            boolean branch = random.nextBoolean();
            arguments.put(DEPENDENT_NAME, branch);
            return branch;
        }, dataAmount));

        System.out.println("int identity dependency relation");
        // int identity dependency relation
        OfInt randomSquared = random.ints(1, 100).iterator();
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> {
            int val = randomSquared.nextInt();
            arguments.put(DEPENDENT_NAME, val);
            return val < 10;
        }, dataAmount));

        System.out.println("decimal identity dependency relation");
        // decimal identity dependency relation
        java.util.PrimitiveIterator.OfDouble randomww = random.doubles(-10.0, 1000.0).iterator();
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> {
            double val = randomww.next();
            arguments.put(DEPENDENT_NAME, val);
            return val < 10;
        }, dataAmount));

        System.out.println("string identity dependency relation");
        // string identity dependency relation
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> {
            String val = PossibleStringValues.get(random.nextInt(PossibleStringValues.size()));
            arguments.put(DEPENDENT_NAME, val);
            return val.equals(PossibleStringValues.get(2));
        }, dataAmount));

        System.out.println("Complex fibonacci dependency relation");
        // Complex fibonacci dependency relation
        OfInt randomf = random.ints(100, 10000).iterator();
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> {
            int input = randomf.nextInt();
            long time = average(() -> measureTime(() -> fibonacci(input)), 100);
            arguments.put(DEPENDENT_NAME, input);
            return time > 34485;
        }, dataAmount));

        // Complex scale dependency relation
        branchEvaluation(imageScaleFunctionDataSet.createDataSet((points, dataPoint, dataSet) -> {
            dataSet.addBranch(dataPoint.arguments,
                    dataPoint.responseTime < points.firstFifth ? BRANCH_TRANSITION_ID1 : null);
        }), "./test-data/dependencies/branch-tree.txt");

        
        System.out.println("independent random");
        // independent random
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> random.nextBoolean(), dataAmount));

        System.out.println("independent constant");
        // independent constant
        branchEvaluation(generateBranchData((Map<String, Object> arguments) -> true, 100));
    }

    private static void loopEval() throws Exception {
        Random random = new Random(0);
        int dataAmount = 100;

        // Identity dependency relation
        loopEvaluation(generateLoopData((Map<String, Object> arguments) -> {
            int iterations = random.nextInt();
            arguments.put(DEPENDENT_NAME, iterations);
            arguments.put(MISLEAD_DEPENDENT_NAME, iterations * 2.5);
            return iterations;
        }, dataAmount));

        // squared dependency relation
        OfInt randomSquared = random.ints(1, 100).iterator();
        loopEvaluation(generateLoopData((Map<String, Object> arguments) -> {
            int iterations = randomSquared.nextInt();
            arguments.put(DEPENDENT_NAME, iterations);
            return (int) Math.pow(iterations, 2.0);
        }, dataAmount));

        // cubic dependency relation
        OfInt randomCubic = random.ints(1, 10).iterator();
        loopEvaluation(generateLoopData((Map<String, Object> arguments) -> {
            int iterations = randomCubic.nextInt();
            arguments.put(DEPENDENT_NAME, iterations);
            return (int) Math.pow(iterations, 3.0);
        }, dataAmount));

        // Complex fibonacci dependency relation
        CsvWriter csv = new CsvWriter("./test-data/dependencies/loop-fibonacci-input.csv", "input", "iterations");
        OfInt randomf = random.ints(100, 10000).iterator();
        loopEvaluation(generateLoopData((arguments) -> {
            int input = randomf.nextInt();
            long time = average(() -> measureTime(() -> fibonacci(input)), 100);
            arguments.put(DEPENDENT_NAME, input);
            int iterations = (int) time;
            csv.write(input, iterations);
            return iterations;
        }, dataAmount));
        csv.close();

        // Complex scale dependency relation
        /*CsvWriter csv2 = new CsvWriter("./test-data/dependencies/loop-image-input.csv",
                imageScaleFunction.getArguments(), "iterations");
        loopEvaluation(imageScaleFunctionDataSet.createDataSet((points, dataPoint, dataSet) -> {
            long iterations = (long) (points.factor * dataPoint.responseTime * 100);
            dataSet.addLoop(dataPoint.arguments, dataPoint.responseTime);
            csv2.write(dataPoint.arguments, dataPoint.responseTime);
        }));
        csv2.close();*/

        // independent random
        loopEvaluation(generateLoopData((Map<String, Object> arguments) -> random.nextInt(), dataAmount));

        // independent constant
        loopEvaluation(generateLoopData((Map<String, Object> arguments) -> DEPENDENT_CONST_INT, dataAmount));
    }

    private static void rdEval() throws Exception {
        Random random = new Random(0);
        int dataAmount = 100;

        // Identity dependency relation
        resourceDemandEvaluation(generateResourceDemandData(arguments -> {
            double iterations = random.nextDouble();
            arguments.put(DEPENDENT_NAME, iterations);
            return iterations;
        }, dataAmount));

        // squared dependency relation
        OfInt randomSquared = random.ints(1, 100).iterator();
        resourceDemandEvaluation(generateResourceDemandData(arguments -> {
            int iterations = randomSquared.nextInt();
            arguments.put(DEPENDENT_NAME, iterations);
            return Math.pow(iterations, 2.0);
        }, dataAmount));

        // cubic dependency relation
        OfInt randomCubic = random.ints(1, 10).iterator();
        resourceDemandEvaluation(generateResourceDemandData(arguments -> {
            int iterations = randomCubic.nextInt();
            arguments.put(DEPENDENT_NAME, iterations);
            return Math.pow(iterations, 3.0);
        }, dataAmount));

        // Complex fibonacci dependency relation
        OfInt randomf = random.ints(100, 10000).iterator();
        resourceDemandEvaluation(generateResourceDemandData(arguments -> {
            int input = randomf.nextInt();
            long time = average(() -> measureTime(() -> fibonacci(input)), 100);
            arguments.put(DEPENDENT_NAME, input);
            double iterations = (double) time;
            return iterations;
        }, dataAmount));

        // Complex scale dependency relation
        resourceDemandEvaluation(imageScaleFunctionDataSet.createDataSet((points, dataPoint) -> {
            return (double) dataPoint.responseTime;
        }));

        // independent random
        resourceDemandEvaluation(generateResourceDemandData(arguments -> random.nextDouble(), dataAmount));

        // independent constant
        resourceDemandEvaluation(generateResourceDemandData(arguments -> DEPENDENT_CONST_DOUBLE, dataAmount));
    }

    private static EvaluationMonitoringDataSet generateBranchData(Function<Map<String, Object>, Boolean> transition,
            int amount) throws Exception {
        EvaluationMonitoringDataSet dataSet = new EvaluationMonitoringDataSet();
        Random random = new Random(0);

        for (int i = 0; i < amount; i++) {
            Map<String, Object> arguments = createIndependent(random);
            dataSet.addBranch(arguments, transition.apply(arguments) ? BRANCH_TRANSITION_ID1 : null);
        }
        return dataSet;
    }

    private static void branchEvaluation(EvaluationMonitoringDataSet dataSet) throws Exception {
        branchEvaluation(dataSet, null);
    }

    private static void branchEvaluation(EvaluationMonitoringDataSet dataSet, String graphFilePath) throws Exception {
        TreeWekaBranchModelEstimation estimation = new TreeWekaBranchModelEstimation(dataSet.getServiceCalls(),
                dataSet.getBranches());

        BranchModel model = estimation.estimate(Common.DEFAULT_MODEL_ID);
        if (model instanceof WekaBranchModel) {
            WekaBranchModel branchModel = (WekaBranchModel) model;
            Evaluation evaluation = new Evaluation(branchModel.getDataset());
            evaluation.crossValidateModel(branchModel.getClassifier(), branchModel.getDataset(), 10, new Random(0));

            System.out.println(evaluation.toSummaryString());

            int index = branchModel.getDataset().classAttribute().indexOfValue(BRANCH_TRANSITION_ID1);
            double tp = evaluation.numTruePositives(index);
            double tn = evaluation.numTrueNegatives(index);
            double fp = evaluation.numFalsePositives(index);
            double fn = evaluation.numFalseNegatives(index);
            double precision2 = evaluation.precision(index);
            double recall2 = evaluation.recall(index);
            System.out.printf(Locale.ROOT,
                    "\n%d & %d & %d & %d & %.3f & %.3f \\\\\n\n",
                    (int)tp,
                    (int)tn,
                    (int)fp,
                    (int)fn, precision2, recall2);

            // visualizeTree(branchModel.getClassifier());
            if (graphFilePath != null) {
                String asd = branchModel.getClassifier().graph();
                Files.write(Paths.get(graphFilePath), asd.getBytes(),
                        StandardOpenOption.CREATE);
            }

           
            //printErrors(branchModel.getClassifier(), branchModel.getDataset());
        }

        System.out.println(model.getBranchStochasticExpression(BRANCH_TRANSITION_ID1));
    }

    private static void visualizeTree(J48 tree) throws Exception {
        // display classifier
        final javax.swing.JFrame jf = new javax.swing.JFrame("Weka Classifier Tree Visualizer: J48");
        jf.setSize(500, 400);
        jf.getContentPane().setLayout(new BorderLayout());
        TreeVisualizer tv = new TreeVisualizer(null,
                tree.graph(),
                new PlaceNode2());
        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                jf.dispose();
            }
        });

        jf.setVisible(true);
        tv.fitToScreen();
    }

    private static EvaluationMonitoringDataSet generateLoopData(Function<Map<String, Object>, Integer> iterations,
            int amount) throws Exception {
        EvaluationMonitoringDataSet dataSet = new EvaluationMonitoringDataSet();
        Random random = new Random(0);

        for (int i = 0; i < amount; i++) {
            Map<String, Object> arguments = createIndependent(random);
            dataSet.addLoop(arguments, iterations.apply(arguments));
        }
        return dataSet;
    }

    private static void loopEvaluation(EvaluationMonitoringDataSet dataSet) throws Exception {
        WekaLoopModelEstimation estimation = new WekaLoopModelEstimation(dataSet.getServiceCalls(), dataSet.getLoops());
        WekaLoopModel loopModel = (WekaLoopModel) estimation.estimate(Common.DEFAULT_MODEL_ID);
        Evaluation evaluation = new Evaluation(loopModel.getDataset());
        evaluation.crossValidateModel(loopModel.getClassifier(), loopModel.getDataset(), 10, new Random(0));
        System.out.println(evaluation.toSummaryString());
        System.out.println(loopModel.getIterationsStochasticExpression());
        // printErrors(loopModel.getClassifier(), loopModel.getDataset());
    }

    private static Map<ServiceParameters, Double> generateResourceDemandData(
            Function<Map<String, Object>, Double> iterations,
            int amount) throws Exception {
        Map<ServiceParameters, Double> dataSet = new HashMap<ServiceParameters, Double>();
        Random random = new Random(0);

        for (int i = 0; i < amount; i++) {
            Map<String, Object> arguments = createIndependent(random);
            Double value = iterations.apply(arguments);
            dataSet.put(ServiceParameters.build(arguments), value);
        }
        return dataSet;
    }

    private static void resourceDemandEvaluation(Map<ServiceParameters, Double> dataSet) throws Exception {
        WekaParametricDependencyEstimationStrategy estimation = new WekaParametricDependencyEstimationStrategy();
        ResourceDemandModel model = estimation.estimateResourceDemandModel(Common.DEFAULT_MODEL_ID,
                Common.DEFAULT_MODEL_ID, dataSet);
        if (model instanceof WekaResourceDemandModel) {
            WekaResourceDemandModel rdModel = (WekaResourceDemandModel) model;
            Evaluation evaluation = new Evaluation(rdModel.getDataset());
            evaluation.crossValidateModel(rdModel.getClassifier(), rdModel.getDataset(), 10, new Random(0));
            System.out.println(evaluation.toSummaryString());
        }

        System.out.println(model.getResourceDemandStochasticExpression());
        // printErrors(loopModel.getClassifier(), loopModel.getDataset());
    }

    private static void printErrors(Classifier classifier, Instances dataSet) throws Exception {
        for (Instance instance : dataSet) {
            double clazz = classifier.classifyInstance(instance);
            if (clazz != instance.classValue()) {
                System.out.print("False classification: ");
                System.out.print("expected: ");
                System.out.print(clazz);
                System.out.print(" was: ");
                System.out.print(instance.classValue());
                System.out.print(" instance: ");
                System.out.print(instance.toString());
                System.out.println();
            }
        }
    }

    private static Map<String, Object> createIndependent(Random random) {
        Map<String, Object> arguments = new HashMap<String, Object>();
        arguments.put(CONST_INT_NAME, CONST_INT);
        arguments.put(CONST_STRING_NAME, CONST_STRING);
        arguments.put(CONST_DECIMAL_NAME, CONST_DECIMAL);
        arguments.put(CONST_BOOLEAN_NAME, CONST_BOOLEAN);

        arguments.put(RANDOM_INT_NAME, random.nextInt());
        arguments.put(RANDOM_STRING_NAME, PossibleStringValues.get(random.nextInt(PossibleStringValues.size())));
        arguments.put(RANDOM_DECIMAL_NAME, random.nextDouble());
        arguments.put(RANDOM_BOOLEAN_NAME, random.nextBoolean());
        return arguments;
    }

    public static long average(LongSupplier action, int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += action.getAsLong();
        }
        return sum / iterations;
    }

    public static long measureTime(Action action) {
        long startTime = System.nanoTime();
        action.execute();
        long stopTime = System.nanoTime();
        long timeDiff = stopTime - startTime;
        return timeDiff;
    }

    public static class FunctionDataPoint {
        Map<String, Object> arguments;
        long responseTime;

        public FunctionDataPoint(Map<String, Object> arguments, long responseTime) {
            this.arguments = arguments;
            this.responseTime = responseTime;
        }
    }

    public static class FunctionDataSet {
        List<FunctionDataPoint> result;

        long min = Long.MAX_VALUE;

        long max = Long.MIN_VALUE;

        long firstFifth;

        long diff;

        double factor;

        public FunctionDataSet(List<FunctionDataPoint> result) {
            this.result = result;
            for (FunctionDataPoint functionDataPoint : result) {
                max = Math.max(max, functionDataPoint.responseTime);
                min = Math.min(min, functionDataPoint.responseTime);
            }

            diff = (max - min);
            firstFifth = diff / 5 + min;
            factor = 1.0 / diff;
        }

        public EvaluationMonitoringDataSet createDataSet(
                TriConsumer<FunctionDataSet, FunctionDataPoint, EvaluationMonitoringDataSet> valueAdder) {
            EvaluationMonitoringDataSet dataSet = new EvaluationMonitoringDataSet();
            for (FunctionDataPoint dataPoint : result) {
                valueAdder.accept(this, dataPoint, dataSet);
            }
            return dataSet;
        }

        public Map<ServiceParameters, Double> createDataSet(
                BiFunction<FunctionDataSet, FunctionDataPoint, Double> valueAdder) {
            Map<ServiceParameters, Double> dataSet = new HashMap<ServiceParameters, Double>();
            for (FunctionDataPoint dataPoint : result) {
                Double val = valueAdder.apply(this, dataPoint);
                dataSet.put(ServiceParameters.build(dataPoint.arguments), val);
            }
            return dataSet;
        }

    }

    public static FunctionDataSet generateFunctionData(Function<Map<String, Object>, Integer> generate,
            int amount) throws Exception {
        Random random = new Random(0);
        List<FunctionDataPoint> data = new ArrayList<FunctionDataPoint>();
        for (int i = 0; i < amount; i++) {
            Map<String, Object> arguments = createIndependent(random);
            Integer res = generate.apply(arguments);
            data.add(new FunctionDataPoint(arguments, res));
        }
        return new FunctionDataSet(data);
    }

    public static class ImageScaleFunction {

        public List<String> getArguments() {
            List<String> args = new ArrayList<String>();
            args.add(DEPENDENT_NAME + "_ImageType");
            args.add(DEPENDENT_NAME + "_dH");
            args.add(DEPENDENT_NAME + "_dW");
            args.add(DEPENDENT_NAME + "_dfH");
            args.add(DEPENDENT_NAME + "_dfW");
            return args;
        }

        public FunctionDataSet generateImageScale(int dataAmount) throws Exception {
            Random random = new Random(0);

            FunctionDataSet result = generateFunctionData((Map<String, Object> arguments) -> {
                int imageType = PossibleImageTypeValues.get(random.nextInt(PossibleImageTypeValues.size()));
                int dH = random.nextInt(2000) + 100;
                int dW = random.nextInt(2000) + 100;
                double dfH = random.nextInt(3) + 1;
                double dfW = random.nextInt(3) + 1;
                long time = average(() -> measureTime(() -> scaleImage(RandomImage, imageType, dH, dW, dfH, dfW)), 100);
                arguments.put(DEPENDENT_NAME + "_ImageType", imageType);
                arguments.put(DEPENDENT_NAME + "_dH", dH);
                arguments.put(DEPENDENT_NAME + "_dW", dW);
                arguments.put(DEPENDENT_NAME + "_dfH", dfH);
                arguments.put(DEPENDENT_NAME + "_dfW", dfW);
                int iterations = (int) time;
                return iterations;
            }, dataAmount);
            return result;
        }

        /**
         * scale image
         * 
         * @param sbi
         *            image to scale
         * @param imageType
         *            type of image
         * @param dWidth
         *            width of destination image
         * @param dHeight
         *            height of destination image
         * @param fWidth
         *            x-factor for transformation / scaling
         * @param fHeight
         *            y-factor for transformation / scaling
         * @return scaled image
         */
        public static BufferedImage scaleImage(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth,
                double fHeight) {
            BufferedImage dbi = null;
            if (sbi != null) {
                dbi = new BufferedImage(dWidth, dHeight, imageType);
                Graphics2D g = dbi.createGraphics();
                AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
                g.drawRenderedImage(sbi, at);
            }
            return dbi;
        }

        private static final BufferedImage RandomImage = createRandomImage();

        public static BufferedImage createRandomImage() {
            Random rnd = new Random(0);
            BufferedImage image;
            image = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_BGR);
            int[] array = new int[image.getWidth() * image.getHeight() * image.getRaster().getNumBands()];
            for (int i = 0; i < array.length; ++i) {
                array[i] = rnd.nextInt(0xFF);
            }
            image.getRaster().setPixels(0, 0, image.getWidth(), image.getHeight(), array);
            return image;
        }
    }

    public static int fibonacci(int n) {
        int f = 0, g = 1;

        for (int i = 1; i <= n; i++) {
            f = f + g;
            g = f - g;
        }
        return f;
    }

}
