package evaluation.casestudy;

import evaluation.Action;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class A {

    private final B externalB;
    private final Common c;

    public A(final B externalB, Common common) {
        this.externalB = externalB;
        this.c = common;
    }

    public int methodA(final int a) {
        c.whenOn(() -> {
            ServiceParameters serviceParameters = new ServiceParameters();
            serviceParameters.addInt("a", a);
            ThreadMonitoringController.getInstance().enterService("_XYJcUMjPEeiWRYm1yDC5rQ", serviceParameters);
        });
        try {
            return this.methodAIntern(a);
        } finally {
            c.whenOn(() -> {
                ThreadMonitoringController.getInstance().exitService();
            });
        }
    }

    private int methodAIntern(final int a) {
        c.logOnIteration(Mode.Iteration0, "_OkrUMMjSEeiWRYm1yDC5rQ",
                () -> Common.computation(a));

        // Monitoring actions start
        long ___loopIterationsCount_1 = 0;
        // Monitoring actions end

        int result = 0;
        for (int i = 0; i < a; i++) {

            // Monitoring actions start
            ___loopIterationsCount_1++;
            // Monitoring actions end

            c.logOnIteration(Mode.Iteration0, "_dhstIMjSEeiWRYm1yDC5rQ",
                    () -> Common.computation(Common.ComputationConst1));

            // Monitoring actions start
            String ___executedBranchId_1 = null;
            // Monitoring actions end

            if (a > 5) {
                // Monitoring actions start
                ___executedBranchId_1 = "_HbtQ8MjTEeiWRYm1yDC5rQ";
                // Monitoring actions end

                c.setCallerId("_P1p-cMjTEeiWRYm1yDC5rQ");
                result += this.externalB.methodB1();
            }

            c.logBranchOnIteration(Mode.Iteration0, "_VTWLoMjSEeiWRYm1yDC5rQ", ___executedBranchId_1);
        }

        c.logOnIteration(Mode.Iteration0, "_SSitEMjSEeiWRYm1yDC5rQ", ___loopIterationsCount_1);

        if (c.mustRun(Mode.Iteration1)) {
            
            // Monitoring actions start
            String ___executedBranchId_2 = null;
            // Monitoring actions end

            if (a < 7) {

                // Monitoring actions start
                ___executedBranchId_2 = "_Lakg4MwNEeiWXYGpzxFH0A";
                // Monitoring actions end

                c.setCallerId("_Sd2WYMwNEeiWXYGpzxFH0A");
                this.externalB.methodB2();
            }

            c.logBranchOnIteration(Mode.Iteration1, "_8icPAMwMEeiWXYGpzxFH0A", ___executedBranchId_2);
        }

        return result;
    }

}
