package tools.vitruv.applications.pcmjava.modelrefinement.parameters.pipeline.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class LongDistribution {

	private List<Long> values;

	public LongDistribution() {
		this.values = new ArrayList<>();
	}

	public void addValue(long val) {
		values.add(val);
	}

	public double ksTest(LongDistribution other) {
		Collections.sort(other.values);
		Collections.sort(this.values);

		KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		return test.kolmogorovSmirnovStatistic(this.values.stream().mapToDouble(l -> (double) l).toArray(),
				other.values.stream().mapToDouble(l -> (double) l).toArray());
	}

	public double avg() {
		return values.stream().mapToDouble(l -> (double) l).average().getAsDouble();
	}

}
