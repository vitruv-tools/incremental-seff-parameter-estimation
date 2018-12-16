package evaluation.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCallDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceParameters;

public class ServiceCallDataSetImpl implements ServiceCallDataSet {

    private static double TIME_TO_SECONDS = 1.0e-9;

    private final Map<String, ServiceCall> serviceExecutionIdToCall;
    private final Map<String, ArrayList<ServiceCall>> serviceIdToCall;
    private final Map<String, Map<String, ArrayList<ServiceCall>>> callerIdToServiceIdToCall;
    private final List<ServiceCall> allServiceCalls;

    public ServiceCallDataSetImpl() {
        this.serviceExecutionIdToCall = new HashMap<>();
        this.serviceIdToCall = new HashMap<>();
        this.callerIdToServiceIdToCall = new HashMap<>();
        this.allServiceCalls = new ArrayList<>();
    }

    @Override
    public ServiceParameters getParametersOfServiceCall(final String serviceExecutionId) {
        ServiceCall executionItem = this.serviceExecutionIdToCall.get(serviceExecutionId);
        if (executionItem == null) {
            throw new IllegalArgumentException(
                    String.format("The service call with id %s does not exist.", serviceExecutionId));
        }
        return executionItem.getParameters();
    }

    @Override
    public List<ServiceCall> getServiceCalls() {
        return this.allServiceCalls;
    }

    @Override
    public List<ServiceCall> getServiceCalls(final String serviceId) {
        return this.serviceIdToCall.get(serviceId);
    }

    @Override
    public Set<String> getServiceIds() {
        return this.serviceIdToCall.keySet();
    }

    @Override
    public Set<String> getServiceIdsForExternalCallId(final String externalCallId) {
        Map<String, ArrayList<ServiceCall>> serviceIdToCalls = this.callerIdToServiceIdToCall.get(externalCallId);
        if (serviceIdToCalls == null) {
            return Collections.emptySet();
        }
        return serviceIdToCalls.keySet();
    }

    @Override
    public double timeToSeconds(final long time) {
        return time * TIME_TO_SECONDS;
    }

    protected void add(ServiceCall serviceCall) {
        this.allServiceCalls.add(serviceCall);

        this.serviceExecutionIdToCall.put(serviceCall.getServiceExecutionId(), serviceCall);

        ArrayList<ServiceCall> serviceIdToCallList = this.serviceIdToCall.get(serviceCall.getServiceId());
        if (serviceIdToCallList == null) {
            serviceIdToCallList = new ArrayList<>();
            this.serviceIdToCall.put(serviceCall.getServiceId(), serviceIdToCallList);
        }
        serviceIdToCallList.add(serviceCall);

        if (serviceCall.getCallerId().isPresent()) {
            Map<String, ArrayList<ServiceCall>> callerIdToServiceId = this.callerIdToServiceIdToCall
                    .get(serviceCall.getCallerId().get());
            if (callerIdToServiceId == null) {
                callerIdToServiceId = new HashMap<>();
                this.callerIdToServiceIdToCall.put(serviceCall.getCallerId().get(), callerIdToServiceId);
            }

            ArrayList<ServiceCall> callerIdCalls = callerIdToServiceId.get(serviceCall.getServiceId());
            if (callerIdCalls == null) {
                callerIdCalls = new ArrayList<>();
                callerIdToServiceId.put(serviceCall.getServiceId(), callerIdCalls);
            }
            callerIdCalls.add(serviceCall);
        }
    }

    protected static class ServiceCallImpl implements ServiceCall {

        private final Optional<String> callerId;
        private final String callerServiceExecutionId;
        private final long entryTime;
        private final long exitTime;
        private final ServiceParameters parameters;
        private final String serviceId;
        private final String serviceExecutionId;
        private final String sessionId;

        public ServiceCallImpl(Optional<String> callerId, String callerServiceExecution, long entryTime, long exitTime,
                ServiceParameters parameters, String serviceId, String sessionId, String serviceExecutionId) {
            this.callerId = callerId;
            this.callerServiceExecutionId = callerServiceExecution;
            this.entryTime = entryTime;
            this.exitTime = exitTime;
            this.parameters = parameters;
            this.serviceId = serviceId;
            this.serviceExecutionId = serviceExecutionId;
            this.sessionId = sessionId;
        }

        @Override
        public Optional<String> getCallerId() {
            return this.callerId;
        }

        @Override
        public String getCallerServiceExecutionId() {
            return this.callerServiceExecutionId;
        }

        @Override
        public long getEntryTime() {
            return this.entryTime;
        }

        @Override
        public long getExitTime() {
            return this.exitTime;
        }

        @Override
        public ServiceParameters getParameters() {
            return this.parameters;
        }

        @Override
        public long getResponseTime() {
            return this.exitTime - this.entryTime;
        }

        @Override
        public double getResponseTimeSeconds() {
            return this.getResponseTime() * TIME_TO_SECONDS;
        }

        @Override
        public String getServiceExecutionId() {
            return this.serviceExecutionId;
        }

        @Override
        public String getServiceId() {
            return this.serviceId;
        }

        @Override
        public double timeToSeconds(final long time) {
            return time * TIME_TO_SECONDS;
        }

        @Override
        public String getSessionId() {
            return this.sessionId;
        }
    }
}
