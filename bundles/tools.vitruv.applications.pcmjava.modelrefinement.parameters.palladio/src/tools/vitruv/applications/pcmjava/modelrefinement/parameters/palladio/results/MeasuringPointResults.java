package tools.vitruv.applications.pcmjava.modelrefinement.parameters.palladio.results;

import java.util.List;
import java.util.stream.Collectors;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

public class MeasuringPointResults {
	private List<Measure<Double, Duration>> xValues;
	private List<Measure<Double, Duration>> yValues;

	public MeasuringPointResults() {
		this.xValues = null;
		this.yValues = null;
	}

	public void applyLongValues(List<Measure<Long, Duration>> values) {
		this.applyDoubleValues(values.parallelStream().map(ms -> Measure.valueOf((double) ms.getValue(), ms.getUnit()))
				.collect(Collectors.toList()));
	}

	public void applyDoubleValues(List<Measure<Double, Duration>> values) {
		if (yValues == null) {
			yValues = values;
		} else {
			xValues = yValues;
			yValues = values;
		}
	}

	public List<Measure<Double, Duration>> getXValues() {
		return xValues;
	}

	public List<Measure<Double, Duration>> getYValues() {
		return yValues;
	}

}
