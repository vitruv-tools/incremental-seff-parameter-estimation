package tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Triple;
import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.parameter.ParameterFactory;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableCharacterisationType;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

import de.uka.ipd.sdq.stoex.StoexFactory;
import de.uka.ipd.sdq.stoex.VariableReference;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.ServiceCall;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.ServiceCallBundle;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.AbstractUsageElement;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.UsageCallStructure;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.data.usage.UsageLoopStructure;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.usagemodel.mapping.MonitoringDataMapping;
import tools.vitruv.applications.pcmjava.modelrefinement.parameters.util.PcmUtils;

public class UsageSession {
	private static final String CALLER_NOT_SET = "<not set>";

	private List<ServiceCall> calls;

	public UsageSession() {
		calls = new ArrayList<>();
	}

	public void update(ServiceCall call) {
		if (call.getCallerServiceExecutionId().equals(CALLER_NOT_SET)) {
			// => ENTRY CALL
			calls.add(call);
		}
	}

	public void compress() {
		// sort before
		sortCalls();

		// bundle calls
		List<ServiceCallBundle> bundles = bundleCalls();

		// find structure
		buildUsage(bundles, findRepeatingBundles(bundles));
	}

	private List<AbstractUsageElement> buildUsage(List<ServiceCallBundle> bundles,
			List<Triple<Integer, Integer, Integer>> patterns) {
		final List<AbstractUsageElement> els = new ArrayList<>();

		// build whole structure
		for (ServiceCallBundle bundle : bundles) {
			if (bundle.isLoop()) {
				UsageLoopStructure loop = new UsageLoopStructure(bundle.getCallCount());
				loop.addChild(new UsageCallStructure(bundle.getServiceCall()));
				els.add(loop);
			} else {
				els.add(new UsageCallStructure(bundle.getServiceCall()));
			}
		}

		// merge structures
		final List<Integer> removes = new ArrayList<>();
		for (Triple<Integer, Integer, Integer> pattern : patterns) {
			// merge elements
			int patternSize = pattern.getMiddle() - pattern.getLeft() + 1;
			// [a, b, a, b, a, b] => pattern start is where the first repetition starts ( at
			// second a ) and the end is at the end of the pattern
			final int refPatternStart = pattern.getLeft();
			final int patternStart = pattern.getLeft() + patternSize;
			final int patternEnd = patternStart + pattern.getRight() * patternSize;

			for (int cursor = patternStart; cursor < patternEnd; cursor += patternSize) {
				final int cursorCopy = cursor;

				IntStream.range(cursor, cursor + patternSize).forEach(k -> {
					int offset = k - cursorCopy;
					els.get(refPatternStart + offset).merge(els.get(k));
					removes.add(k);
				});
			}

			// we need to build a loop out of these elements
			// TODO problem is that the loop moves the indices
			UsageLoopStructure newLoop = new UsageLoopStructure(pattern.getRight());
			for (int z = refPatternStart; z < refPatternStart + patternSize; z++) {
				newLoop.addChild(els.get(z));

				if (z != refPatternStart) {
					removes.add(z);
				}
			}
			els.set(refPatternStart, newLoop); // replace here this doesnt changes the indices and that is imp
		}

		// we need to reverse the list so we dont need to modify the indices
		Collections.sort(removes, Collections.reverseOrder());
		for (int k : removes) {
			els.remove(k);
		}

		return els;
	}

	/**
	 * Finds repeating structures in the service calls. Looks very complex but i
	 * think its O(n^2) -> could be worse :)
	 * 
	 * @param bundles
	 *            bundled service calls (sorted)
	 * @return triple of integers (start, end, repetitions) of the certain structure
	 */
	private List<Triple<Integer, Integer, Integer>> findRepeatingBundles(List<ServiceCallBundle> bundles) {
		List<Triple<Integer, Integer, Integer>> ret = new ArrayList<>();

		for (int k = 0; k < bundles.size(); k++) {
			// bundles should be an arraylist otherwise this is very inefficient
			ServiceCallBundle bundle = bundles.get(k);

			int nextOcc = -1;
			for (int j = k + 1; j < bundles.size(); j++) {
				if (bundles.get(j).canBeBundled(bundle)) {
					// this is the next occurrence
					nextOcc = j;
					break;
				}
			}

			// counts following occurences
			int upcomingOccurences = 0;
			if (nextOcc >= 0) {
				int patternLength = nextOcc - k;
				for (int z = k + patternLength; z < bundles.size(); z += patternLength) {
					final int kCopy = k;
					final int zCopy = z;

					boolean patternMatching = IntStream.range(z, z + patternLength).allMatch(index -> {
						return bundles.get(index).canBeBundled(bundles.get(kCopy + (index - zCopy)));
					});

					if (patternMatching) {
						upcomingOccurences++;
					} else {
						break;
					}
				}

				if (upcomingOccurences > 0) {
					ret.add(Triple.of(k, nextOcc - 1, upcomingOccurences));

					k += (upcomingOccurences + 1) * patternLength;
					k--; // because ++ comes afterwards
				}
			}
		}

		return ret;
	}

	private List<ServiceCallBundle> bundleCalls() {
		List<ServiceCallBundle> bundles = new ArrayList<>();

		ServiceCallBundle currentBundle = null;
		for (ServiceCall call : calls) {
			if (currentBundle == null) {
				currentBundle = new ServiceCallBundle(call);
			} else {
				if (currentBundle.canBeBundled(call)) {
					currentBundle.bundle(call);
				} else {
					bundles.add(currentBundle);
					currentBundle = new ServiceCallBundle(call);
				}
			}
		}

		if (currentBundle != null) {
			bundles.add(currentBundle);
		}

		return bundles;
	}

	// TODO an improvement would be to automatically build loops and branches
	// but => lower accuracy and much more effort (for small sessions not relevant)
	// TODO outsource the chaining
	public ScenarioBehaviour toBehaviour(Repository belRepository, MonitoringDataMapping mapping) {
		// calls need to be sorted
		this.sortCalls();

		// do the derivation of the Behaviour
		ScenarioBehaviour build = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		AbstractUserAction current = start;
		long currentTime = 0;

		build.getActions_ScenarioBehaviour().add(start);

		for (ServiceCall call : calls) {
			// build delay
			if (current != start) {
				// the int cast is perfectly fine, we dont care about +- 1ms
				int delayTime = (int) ((call.getEntryTime() - currentTime) / 1000000L);
				Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
				delay.setTimeSpecification_Delay(buildIntLiteral(delayTime));

				// chaining
				current.setSuccessor(delay);
				delay.setPredecessor(current);
				build.getActions_ScenarioBehaviour().add(delay);
				current = delay;
			}

			// get seff
			ResourceDemandingSEFF seff = PcmUtils.resolveSEFF(belRepository, call.getServiceId());

			// build entry call
			EntryLevelSystemCall entryCall = UsagemodelFactory.eINSTANCE.createEntryLevelSystemCall();
			entryCall
					.setOperationSignature__EntryLevelSystemCall((OperationSignature) seff.getDescribedService__SEFF());
			entryCall.setProvidedRole_EntryLevelSystemCall(PcmUtils.getProvidedRole(seff));

			// parameter build
			if (call.getParameters() != null) {
				for (Entry<String, Object> parameter : call.getParameters().getParameters().entrySet()) {
					if (mapping.hasSEFFParameterName(parameter.getKey())) {
						// we need to build the parameter
						String belongingSeffParameter = mapping.getSEFFParameterName(parameter.getKey());
						String[] operatorSplit = belongingSeffParameter.split("\\.");

						VariableUsage usage = ParameterFactory.eINSTANCE.createVariableUsage();
						VariableCharacterisation character = ParameterFactory.eINSTANCE
								.createVariableCharacterisation();
						VariableReference reference = StoexFactory.eINSTANCE.createVariableReference();

						reference.setReferenceName(operatorSplit[0]);
						character.setType(VariableCharacterisationType.get(operatorSplit[1]));
						character.setSpecification_VariableCharacterisation(
								buildPCMVariable(operatorSplit[1], parameter.getValue()));
						usage.setNamedReference__VariableUsage(reference);
						usage.getVariableCharacterisation_VariableUsage().add(character);

						entryCall.getInputParameterUsages_EntryLevelSystemCall().add(usage);
					}
				}
			}

			// chaining
			current.setSuccessor(entryCall);
			entryCall.setPredecessor(current);
			build.getActions_ScenarioBehaviour().add(entryCall);

			// update time
			currentTime = call.getEntryTime();

			// finish
			current = entryCall;
		}

		// create stop
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		// final chaining
		stop.setPredecessor(current);
		current.setPredecessor(stop);

		build.getActions_ScenarioBehaviour().add(stop);

		// finally
		return build;
	}

	private PCMRandomVariable buildIntLiteral(int value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();
		ret.setSpecification(String.valueOf(value));
		return ret;
	}

	private PCMRandomVariable buildPCMVariable(String key, Object value) {
		PCMRandomVariable ret = CoreFactory.eINSTANCE.createPCMRandomVariable();

		// TODO too static
		if (key.equals("NUMBER_OF_ELEMENTS")) {
			ret.setSpecification(String.valueOf(value));
		}

		return ret;
	}

	private void sortCalls() {
		calls.sort((a, b) -> {
			if (a.getEntryTime() - b.getEntryTime() > 0) {
				return 1;
			} else if (a.getEntryTime() < b.getEntryTime()) {
				return -1;
			} else {
				return 0;
			}
		});
	}

}
