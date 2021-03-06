package tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.dataset;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.dataset.ServiceCallDataSetImpl.ServiceCallImpl;

public class MockServiceCallDataSet extends ServiceCallDataSetImpl {

    public String add(Map<String, Object> serviceArguments) {
        return this.add(serviceArguments, Common.DEFAULT_SERVICE_ID, Common.TIME_NOT_SET, Common.TIME_NOT_SET,
                Common.FIELD_NOT_SET, Common.FIELD_NOT_SET);
    }

    public String add(Map<String, Object> serviceArguments, String serviceId, long entryTime, long exitTime,
            String callerId, String callerExecutionId) {
        String executionId = UUID.randomUUID().toString();
        ServiceCallImpl serviceCall = new ServiceCallImpl(Optional.ofNullable(callerId), callerExecutionId, entryTime,
                exitTime, ServiceParameters.build(serviceArguments), serviceId, executionId);
        super.add(serviceCall);
        return executionId;
    }
}
