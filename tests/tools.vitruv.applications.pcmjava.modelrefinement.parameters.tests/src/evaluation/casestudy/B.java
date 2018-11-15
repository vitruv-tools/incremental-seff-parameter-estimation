package evaluation.casestudy;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.monitoring.ThreadMonitoringController;

public class B {

    private final Common c;

    public B(Common c) {
        this.c = c;
    }

    public int methodB1() {
        c.whenOn(() -> {
            ThreadMonitoringController.getInstance().enterService("_T_bNAMjPEeiWRYm1yDC5rQ");
            ThreadMonitoringController.getInstance().exitService();
        });
        return 1;
    }

    public void methodB2() {
        c.whenOn(() -> {
            ThreadMonitoringController.getInstance().enterService("_1d51QMwMEeiWXYGpzxFH0A");
        });
        try {
            c.logOnIteration(Mode.Iteration1, "_SANpEMwWEeiWXYGpzxFH0A",
                    () -> Common.computation(Common.ComputationConst2));
        } finally {
            c.whenOn(() -> {
                ThreadMonitoringController.getInstance().exitService();
            });
        }
    }
}
