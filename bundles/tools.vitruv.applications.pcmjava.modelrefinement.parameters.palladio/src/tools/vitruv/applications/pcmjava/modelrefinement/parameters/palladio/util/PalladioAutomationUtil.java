package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.palladiosimulator.edp2.models.measuringpoint.MeasuringPoint;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ServiceEffectSpecification;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class PalladioAutomationUtil {
	private static final Pattern ASSEMBLY_CTX_PATTERN = Pattern.compile("AssemblyCtx: (.*),");
	private static final Pattern ENTRY_LEVEL_SYSTEM_PATTERN = Pattern.compile("EntryLevelSystemCall id: (.*) ");

	public static ServiceEffectSpecification getSeffByMeasuringPoint(UsageModel usageModel, System system,
			MeasuringPoint point, MetricDescription metric) {
		if (getMetricType(metric) == MetricType.RESPONSE_TIME) {
			Matcher assemblyMatcher = ASSEMBLY_CTX_PATTERN.matcher(point.getStringRepresentation());

			if (assemblyMatcher.find()) {
				java.lang.System.out.println(point.getStringRepresentation());
				AssemblyContext ctx = PcmUtils.getElementById(system, AssemblyContext.class, assemblyMatcher.group(1));
			} else {
				// is entry level system call?
				Matcher entryCallMatcher = ENTRY_LEVEL_SYSTEM_PATTERN.matcher(point.getStringRepresentation());
				if (entryCallMatcher.find()) {
					EntryLevelSystemCall entryCall = PcmUtils.getElementById(usageModel, EntryLevelSystemCall.class,
							entryCallMatcher.group(1));
					return PcmUtils.getSeffByProvidedRoleAndSignature(system,
							entryCall.getOperationSignature__EntryLevelSystemCall(),
							entryCall.getProvidedRole_EntryLevelSystemCall());
				}
			}
		}

		return null;
	}

	public static MetricType getMetricType(MetricDescription desc) {
		return MetricType.fromId(desc.getId());
	}

}
