package tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.GuardedBranchTransition;
import org.palladiosimulator.pcm.seff.SeffFactory;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.LoggingUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParametersUtil;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl.BranchEstimationImpl;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.branch.impl.WekaBranchModel;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.data.SimpleTestData;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.dataset.Common;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.dataset.MockMonitoringDataSet;

public class BranchEstimationTest {

    private BranchAction mockBranchAction;
    private BranchEstimationImpl branchEstimation;
    private Repository repository;
    private MockMonitoringDataSet dataSet;

    @Before
    public void setUpTest() {
        this.mockBranchAction = this.createBranchAction(Common.DEFAULT_MODEL_ID, Common.MODEL_ID_1);
        this.branchEstimation = new BranchEstimationImpl(new Random(1));
        this.repository = RepositoryFactory.eINSTANCE.createRepository();
        this.dataSet = new MockMonitoringDataSet();
    }

    @Test
    public void estimateBranchExecutedTest() {
        BranchAction branchAction = this.createBranchAction(SimpleTestData.FirstBranchId,
                SimpleTestData.FirstBranchTransitionId);
        MonitoringDataSet reader = SimpleTestData.getReader(SimpleTestData.FirstSessionId);

        this.branchEstimation.update(this.repository, reader.getServiceCalls(), reader.getBranches());

        Optional<AbstractBranchTransition> result = this.branchEstimation.predictTransition(branchAction,
                ServiceParametersUtil.buildServiceCall("a", 6));

        assertTrue(result.isPresent());
        assertEquals(SimpleTestData.FirstBranchTransitionId, result.get().getId());
    }

    @Test
    public void estimateNoBranchExecutedTest() {
        BranchAction branchAction = this.createBranchAction(SimpleTestData.FirstBranchId,
                SimpleTestData.FirstBranchTransitionId);
        MonitoringDataSet reader = SimpleTestData.getReader(SimpleTestData.FirstSessionId);

        this.branchEstimation.update(this.repository, reader.getServiceCalls(), reader.getBranches());

        Optional<AbstractBranchTransition> result = this.branchEstimation.predictTransition(branchAction,
                ServiceParametersUtil.buildServiceCall("a", 1));

        assertFalse(result.isPresent());
    }

    @Test
    public void greaterThanBranchTest() {
        this.addBranchNotExecuted(1.0);
        this.addBranchNotExecuted(2.0);
        this.addBranchNotExecuted(3.0);
        
        this.addBranchExecuted(4.0);
        this.addBranchExecuted(5.0);
        this.addBranchExecuted(6.0);

        this.branchEstimation.update(this.repository, this.dataSet.getServiceCalls(), this.dataSet.getBranches());

        assertEquals("", this.branchEstimation.get(Common.DEFAULT_MODEL_ID).get().getBranchStochasticExpression(Common.MODEL_ID_1));
        
        assertFalse(predictBranch(-10.0).isPresent());
        assertFalse(predictBranch(0.0).isPresent());
        assertFalse(predictBranch(1.0).isPresent());
        assertFalse(predictBranch(3.0).isPresent());

        assertTrue(predictBranch(4.0).isPresent());
        assertTrue(predictBranch(5.0).isPresent());
        assertTrue(predictBranch(10.0).isPresent());
        assertTrue(predictBranch(10.0).isPresent());
    }
    
    @Test
    public void lessThanBranchTest() throws Exception {
        this.addBranchExecuted(1.0);
        this.addBranchExecuted(2.0);
        this.addBranchExecuted(3.0);
        
        this.addBranchNotExecuted(4.0);
        this.addBranchNotExecuted(5.0);
        this.addBranchNotExecuted(6.0);

        this.branchEstimation.update(this.repository, this.dataSet.getServiceCalls(), this.dataSet.getBranches());

        WekaBranchModel branchModel = (WekaBranchModel) this.branchEstimation.get(Common.DEFAULT_MODEL_ID).get();
        String branchModelGraph = branchModel.getClassifier().graph();
        
        assertTrue(predictBranch(-10.0).isPresent());
        assertTrue(predictBranch(0.0).isPresent());
        assertTrue(predictBranch(1.0).isPresent());
        assertTrue(predictBranch(3.0).isPresent());

        assertFalse(predictBranch(4.0).isPresent());
        assertFalse(predictBranch(5.0).isPresent());
        assertFalse(predictBranch(10.0).isPresent());
        assertFalse(predictBranch(10.0).isPresent());
        
        assertEquals("(a <= 3.0 ? true : (a > 3.0 ? false : true ))", branchModel.getBranchStochasticExpression(Common.MODEL_ID_1));
    }

    private Optional<AbstractBranchTransition> predictBranch(Object value) {
        return this.branchEstimation.predictTransition(this.mockBranchAction,
                ServiceParametersUtil.buildServiceCall("a", value));
    }

    private void addBranchNotExecuted(Object value) {
        dataSet.addBranch(Collections.singletonMap("a", value), dataSet.getBranches().getBranchNotExecutedId());
    }

    private void addBranchExecuted(Object value) {
        dataSet.addBranch(Collections.singletonMap("a", value), Common.MODEL_ID_1);
    }

    private BranchAction createBranchAction(String branchId, String transitionId) {
        BranchAction branchAction = SeffFactory.eINSTANCE.createBranchAction();
        branchAction.setId(branchId);
        GuardedBranchTransition branchTransition = SeffFactory.eINSTANCE.createGuardedBranchTransition();
        branchTransition.setId(transitionId);
        branchAction.getBranches_Branch().add(branchTransition);
        return branchAction;
    }

    @BeforeClass
    public static void setUp() {
        LoggingUtil.InitConsoleLogger();
    }
}
