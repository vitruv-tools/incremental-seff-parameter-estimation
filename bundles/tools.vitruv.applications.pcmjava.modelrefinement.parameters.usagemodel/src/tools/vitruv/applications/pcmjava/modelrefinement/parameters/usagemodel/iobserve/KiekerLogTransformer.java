package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.iobserve;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.tuple.Pair;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.flow.trace.operation.AfterOperationEvent;
import kieker.common.record.flow.trace.operation.BeforeOperationEvent;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.core.controller.IMonitoringController;
import kieker.monitoring.core.controller.MonitoringController;
import kieker.monitoring.writer.filesystem.AsciiFileWriter;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.MonitoringDataSet;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;

public class KiekerLogTransformer {

	public KiekerLogTransformer() {
	}

	public void transform(MonitoringDataSet data, CorrespondenceMetadata meta, String outpath) {
		final Configuration configuration = ConfigurationFactory.createDefaultConfiguration();
		configuration.setProperty(ConfigurationFactory.METADATA, "true");
		configuration.setProperty(ConfigurationFactory.AUTO_SET_LOGGINGTSTAMP, "true");
		configuration.setProperty(ConfigurationFactory.WRITER_CLASSNAME, AsciiFileWriter.class.getName());
		configuration.setProperty(ConfigurationFactory.TIMER_CLASSNAME, "kieker.monitoring.timer.SystemMilliTimer");
		configuration.setProperty(AsciiFileWriter.CONFIG_PATH, outpath);

		final IMonitoringController controller = MonitoringController.createInstance(configuration);

		// init tools
		AtomicInteger nextTraceId = new AtomicInteger(0);

		// create
		List<Pair<Long, ServiceCall>> callsSplit = new ArrayList<>();
		for (ServiceCall call : data.getServiceCalls().getServiceCalls()) {
			callsSplit.add(Pair.of(call.getEntryTime(), call));
			callsSplit.add(Pair.of(call.getExitTime(), call));
		}

		// sort calls
		callsSplit.sort((a, b) -> {
			if (a.getKey() > b.getKey()) {
				return 1;
			} else if (a.getKey() < b.getKey()) {
				return -1;
			} else {
				return 0;
			}
		});

		// create records
		Stack<ServiceCall> stack = new Stack<>();
		int traceId = -1;
		int traceOffset = 0;

		for (Pair<Long, ServiceCall> curr : callsSplit) {
			String serviceId = curr.getRight().getServiceId();
			String compId = meta.getComponentByService(serviceId);

			IMonitoringRecord rec;
			if (traceId >= 0) {
				if (stack.peek() == curr.getRight()) {
					rec = new AfterOperationEvent(curr.getLeft(), traceId, traceOffset++, meta.getComponent(compId),
							meta.getOperation(serviceId));
					stack.pop();

					if (stack.empty()) {
						// exit the trace here
						traceId = -1;
					}
				} else {
					rec = new BeforeOperationEvent(curr.getLeft(), traceId, traceOffset++, meta.getComponent(compId),
							meta.getOperation(serviceId));
					stack.push(curr.getRight());
				}
			} else {
				// new trace
				stack.clear();
				traceId = nextTraceId.getAndIncrement();
				traceOffset = 0;
				stack.push(curr.getRight());

				rec = new BeforeOperationEvent(curr.getLeft(), traceId, traceOffset++, meta.getComponent(compId),
						meta.getOperation(serviceId));
			}

			controller.newMonitoringRecord(rec);
		}
	}

}
