package tools.vitruv.applications.pcmjava.modelrefinement.parameters.load.test;

import java.io.File;
import java.io.IOException;

import tools.vitruv.applications.pcmjava.modelrefinement.parameters.load.JMeterExecutor;

public class ExecuteLoadTest {

	@org.junit.Test
	public void test() throws IOException {
		JMeterExecutor exec = new JMeterExecutor(
				"/Users/david/Desktop/Studium/WS1819/Praktikum IngSoftware/Apache JMeter/apache-jmeter-5.0");

		exec.execute(new File("load/load.jmx"));
	}

}
